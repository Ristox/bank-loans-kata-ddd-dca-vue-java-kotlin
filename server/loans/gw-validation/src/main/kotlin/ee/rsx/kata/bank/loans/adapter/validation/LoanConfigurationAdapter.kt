package ee.rsx.kata.bank.loans.adapter.validation

import ee.rsx.kata.bank.loans.domain.limits.LoanLimitsConfig
import ee.rsx.kata.bank.loans.domain.limits.gateway.GetLoanConfig
import jakarta.inject.Named

@Named
internal class LoanConfigurationAdapter : GetLoanConfig {

  override fun invoke() = LoanLimitsConfig(
    MINIMUM_LOAN_AMOUNT,
    MAXIMUM_LOAN_AMOUNT,
    MINIMUM_LOAN_PERIOD_MONTHS,
    MAXIMUM_LOAN_PERIOD_MONTHS
  )

  companion object {
    private const val MINIMUM_LOAN_AMOUNT = 2000
    private const val MAXIMUM_LOAN_AMOUNT = 10000
    private const val MINIMUM_LOAN_PERIOD_MONTHS = 12
    private const val MAXIMUM_LOAN_PERIOD_MONTHS = 60
  }
}
