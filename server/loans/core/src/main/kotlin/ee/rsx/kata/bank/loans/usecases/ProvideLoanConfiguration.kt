package ee.rsx.kata.bank.loans.usecases

import ee.rsx.kata.bank.loans.domain.limits.LoanLimitsConfig
import ee.rsx.kata.bank.loans.domain.limits.gateway.LoanConfigGateway
import ee.rsx.kata.bank.loans.validation.limits.LoadValidationLimits
import ee.rsx.kata.bank.loans.validation.limits.ValidationLimitsDTO
import jakarta.inject.Named

@Named
internal class ProvideLoanConfiguration(
  private val gateway: LoanConfigGateway
) : LoadValidationLimits {

  override fun invoke() = gateway.loadLimits().toDto()

  private fun LoanLimitsConfig.toDto() =
    ValidationLimitsDTO(
      minimumLoanAmount,
      maximumLoanAmount,
      minimumLoanPeriodMonths,
      maximumLoanPeriodMonths
    )
}
