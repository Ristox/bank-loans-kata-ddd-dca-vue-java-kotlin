package ee.rsx.kata.bank.loans.eligibility

fun interface CalculateLoanEligibility {
  fun on(eligibilityRequest: LoanEligibilityRequestDTO): LoanEligibilityResultDTO
}
