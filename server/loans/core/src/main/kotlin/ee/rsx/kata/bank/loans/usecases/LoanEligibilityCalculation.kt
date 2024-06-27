package ee.rsx.kata.bank.loans.usecases

import ee.rsx.kata.bank.loans.domain.limits.gateway.DetermineEligiblePeriod
import ee.rsx.kata.bank.loans.domain.limits.gateway.LoanEligibility
import ee.rsx.kata.bank.loans.domain.segment.CreditSegment
import ee.rsx.kata.bank.loans.domain.segment.gateway.FindCreditSegment
import ee.rsx.kata.bank.loans.domain.ssn.SocialSecurityNumber
import ee.rsx.kata.bank.loans.eligibility.CalculateLoanEligibility
import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityRequestDTO
import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityResultDTO
import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityStatus
import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityStatus.APPROVED
import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityStatus.DENIED
import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityStatus.INVALID
import ee.rsx.kata.bank.loans.extensions.ifTrue
import ee.rsx.kata.bank.loans.validation.limits.LoadValidationLimits
import ee.rsx.kata.bank.loans.validation.limits.ValidationLimitsDTO
import ee.rsx.kata.bank.loans.validation.ssn.ValidateSocialSecurityNumber
import ee.rsx.kata.bank.loans.validation.ssn.ValidationStatus.OK
import jakarta.annotation.Nullable
import jakarta.inject.Named
import kotlin.math.min

@Named
internal class LoanEligibilityCalculation(
  private val validateSocialSecurityNumber: ValidateSocialSecurityNumber,
  private val loadValidationLimits: LoadValidationLimits,
  private val findCreditSegment: FindCreditSegment,
  private val determineEligiblePeriod: DetermineEligiblePeriod
) : CalculateLoanEligibility {

  override fun invoke(eligibilityRequest: LoanEligibilityRequestDTO): LoanEligibilityResultDTO {
    val limits = loadValidationLimits()
    val validationErrors = validate(eligibilityRequest, limits)

    val (status, eligibleAmount, eligiblePeriod) =
      if (validationErrors.isNullOrEmpty()) calculateEligibility(
        eligibilityRequest,
        limits
      ) else
        LoanEligibility(INVALID, null, null)

    return LoanEligibilityResultDTO(
      status,
      validationErrors,
      eligibilityRequest.ssn,
      eligibilityRequest.loanAmount,
      eligibilityRequest.loanPeriodMonths,
      eligibleAmount,
      eligiblePeriod
    )
  }

  @Nullable
  private fun validate(
    eligibilityRequest: LoanEligibilityRequestDTO,
    limits: ValidationLimitsDTO
  ): List<String>? =
    listOfNotNull(
      checkForSsnErrorIn(eligibilityRequest),
      checkForAmountErrorIn(eligibilityRequest, limits),
      checkForPeriodErrorIn(eligibilityRequest, limits)
    )
      .ifEmpty { null }

  private fun checkForSsnErrorIn(request: LoanEligibilityRequestDTO) =
    (validateSocialSecurityNumber(request.ssn).status != OK)
      .ifTrue { "SSN is not valid" }

  private fun checkForAmountErrorIn(request: LoanEligibilityRequestDTO, limits: ValidationLimitsDTO) =
    request.loanAmount.let {
      (it < limits.minimumLoanAmount).ifTrue { "Loan amount is less than minimum required" }
        ?: (it > limits.maximumLoanAmount).ifTrue { "Loan amount is more than maximum allowed" }
    }


  private fun checkForPeriodErrorIn(request: LoanEligibilityRequestDTO, limits: ValidationLimitsDTO) =
    request.loanPeriodMonths.let {
      (it < limits.minimumLoanPeriodMonths).ifTrue { "Loan period is less than minimum required" }
        ?: (it > limits.maximumLoanPeriodMonths).ifTrue { "Loan period is more than maximum allowed" }
    }

  private fun calculateEligibility(request: LoanEligibilityRequestDTO, limits: ValidationLimitsDTO): LoanEligibility {
    val ssn = SocialSecurityNumber(request.ssn)
    val creditSegment: CreditSegment? = findCreditSegment(forPerson = ssn)

    return creditSegment
      ?.let {
        val status = determineEligibilityStatusFor(request, it)

        determineEligibleAmountFor(limits, it, request.loanPeriodMonths)
          ?.let { eligibleAmount ->
            LoanEligibility(status, eligibleAmount)
          }
          ?: run { recalculateEligibilityFor(request, limits, creditSegment, status) }
      }
      ?: LoanEligibility(status = DENIED)
  }

  private fun recalculateEligibilityFor(
    request: LoanEligibilityRequestDTO,
    limits: ValidationLimitsDTO,
    segment: CreditSegment,
    status: LoanEligibilityStatus
  ): LoanEligibility {

    if (segment.isDebt) return LoanEligibility(status)

    val newPeriod = determineEligiblePeriod(forAmount = request.loanAmount, forSegment = segment)
      ?.takeIf {
        it in limits.minimumLoanPeriodMonths..limits.maximumLoanPeriodMonths
      }
    val newAmount = newPeriod?.let { determineEligibleAmountFor(limits, segment, it) }

    return LoanEligibility(status, newAmount, newPeriod)
  }

  private fun determineEligibilityStatusFor(
    request: LoanEligibilityRequestDTO, creditSegment: CreditSegment
  ): LoanEligibilityStatus {
    val creditScore = creditSegment.creditModifier.toDouble() / request.loanAmount * request.loanPeriodMonths

    return if (!creditSegment.isDebt && creditScore > 1)
      APPROVED
    else
      DENIED
  }

  private fun determineEligibleAmountFor(
    limits: ValidationLimitsDTO, creditSegment: CreditSegment, loanPeriodMonths: Int
  ): Int? {
    val eligibleAmount = min(
      limits.maximumLoanAmount.toDouble(),
      (creditSegment.creditModifier * loanPeriodMonths - 1).toDouble()
    ).toInt()

    return (eligibleAmount >= limits.minimumLoanAmount).ifTrue { eligibleAmount }
  }
}
