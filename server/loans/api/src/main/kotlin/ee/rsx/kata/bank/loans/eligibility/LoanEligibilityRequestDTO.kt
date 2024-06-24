package ee.rsx.kata.bank.loans.eligibility

@JvmRecord
data class LoanEligibilityRequestDTO(
  val ssn: String,
  val loanAmount: Int,
  val loanPeriodMonths: Int
)
