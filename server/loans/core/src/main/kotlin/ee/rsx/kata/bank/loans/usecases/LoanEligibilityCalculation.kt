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
import ee.rsx.kata.bank.loans.validation.limits.LoadValidationLimits
import ee.rsx.kata.bank.loans.validation.limits.ValidationLimitsDTO
import ee.rsx.kata.bank.loans.validation.ssn.ValidateSocialSecurityNumber
import ee.rsx.kata.bank.loans.validation.ssn.ValidationStatus.OK
import jakarta.annotation.Nullable
import jakarta.inject.Named
import java.util.*
import java.util.stream.Stream
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
  ): List<String>? {
    val errors = Stream.of(
      checkForSsnErrorIn(eligibilityRequest),
      checkForAmountErrorIn(eligibilityRequest, limits),
      checkForPeriodErrorIn(eligibilityRequest, limits)
    )
      .flatMap { it.stream() }
      .toList()

    return if (errors.isEmpty()) null else errors
  }

  private fun checkForSsnErrorIn(request: LoanEligibilityRequestDTO): Optional<String> {
    val ssnValidity = validateSocialSecurityNumber(request.ssn).status

    return if (ssnValidity == OK)
      Optional.empty()
    else
      Optional.of("SSN is not valid")
  }

  private fun checkForAmountErrorIn(request: LoanEligibilityRequestDTO, limits: ValidationLimitsDTO): Optional<String> {
    val amount = request.loanAmount

    return if (amount < limits.minimumLoanAmount) {
      Optional.of("Loan amount is less than minimum required")
    } else if (amount > limits.maximumLoanAmount) {
      Optional.of("Loan amount is more than maximum allowed")
    } else Optional.empty()
  }

  private fun checkForPeriodErrorIn(request: LoanEligibilityRequestDTO, limits: ValidationLimitsDTO): Optional<String> {
    val period = request.loanPeriodMonths

    return if (period < limits.minimumLoanPeriodMonths) {
      Optional.of("Loan period is less than minimum required")
    } else if (period > limits.maximumLoanPeriodMonths) {
      Optional.of("Loan period is more than maximum allowed")
    } else Optional.empty()
  }

  private fun calculateEligibility(request: LoanEligibilityRequestDTO, limits: ValidationLimitsDTO): LoanEligibility {
    val ssn = SocialSecurityNumber(request.ssn)
    val creditSegment = findCreditSegment(forPerson = ssn)

    return creditSegment
      .map {
        val status = determineEligibilityStatusFor(request, it)

        determineEligibleAmountFor(limits, it, request.loanPeriodMonths)
          .map { eligibleAmount -> LoanEligibility(status, eligibleAmount) }
          .orElseGet { recalculateEligibilityFor(request, limits, it, status) }
      }
      .orElseGet { LoanEligibility(status = DENIED) }
  }

  private fun recalculateEligibilityFor(
    request: LoanEligibilityRequestDTO,
    limits: ValidationLimitsDTO,
    segment: CreditSegment,
    status: LoanEligibilityStatus
  ): LoanEligibility {
    if (segment.isDebt) {
      return LoanEligibility(status)
    }

    val newPeriod = determineEligiblePeriod(forAmount = request.loanAmount, forSegment = segment)
      .filter { limits.minimumLoanPeriodMonths <= it && it <= limits.maximumLoanPeriodMonths }

    val newAmount = newPeriod.flatMap { determineEligibleAmountFor(limits, segment, it) }

    return LoanEligibility(status, newAmount.orElse(null), newPeriod.orElse(null))
  }

  private fun determineEligibilityStatusFor(
    request: LoanEligibilityRequestDTO, creditSegment: CreditSegment
  ): LoanEligibilityStatus {
    val creditScore = creditSegment.creditModifier.toDouble() / request.loanAmount * request.loanPeriodMonths
    return if (!creditSegment.isDebt && creditScore > 1) APPROVED else DENIED
  }

  private fun determineEligibleAmountFor(
    limits: ValidationLimitsDTO, creditSegment: CreditSegment, loanPeriodMonths: Int
  ): Optional<Int> {
    val eligibleAmount = min(
      limits.maximumLoanAmount.toDouble(),
      (creditSegment.creditModifier * loanPeriodMonths - 1).toDouble()
    ).toInt()

    return if (eligibleAmount >= limits.minimumLoanAmount)
      Optional.of(eligibleAmount)
    else
      Optional.empty()
  }
}
