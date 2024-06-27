package ee.rsx.kata.bank.loans.domain.limits

data class LoanLimitsConfig(
  val minimumLoanAmount: Int,
  val maximumLoanAmount: Int,
  val minimumLoanPeriodMonths: Int,
  val maximumLoanPeriodMonths: Int
)
