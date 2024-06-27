package ee.rsx.kata.bank.loans.eligibility

fun interface CalculateLoanEligibility {
  operator fun invoke(eligibilityRequest: LoanEligibilityRequestDTO): LoanEligibilityResultDTO
}
