package ee.rsx.kata.bank.loans.eligibility

data class LoanEligibilityResultDTO(
  val result: LoanEligibilityStatus,
  val errors: List<String>? = null,
  val ssn: String,
  val loanAmount: Int,
  val loanPeriodMonths: Int,
  val eligibleLoanAmount: Int? = null,
  val eligibleLoanPeriod: Int? = null
)
