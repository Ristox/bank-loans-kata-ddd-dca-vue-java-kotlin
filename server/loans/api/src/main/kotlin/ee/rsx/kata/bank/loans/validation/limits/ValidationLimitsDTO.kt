package ee.rsx.kata.bank.loans.validation.limits

data class ValidationLimitsDTO(
  val minimumLoanAmount: Int,
  val maximumLoanAmount: Int,
  val minimumLoanPeriodMonths: Int,
  val maximumLoanPeriodMonths: Int
)
