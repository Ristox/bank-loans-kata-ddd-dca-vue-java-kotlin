package ee.rsx.kata.bank.loans.eligibility;

@FunctionalInterface
public interface CalculateLoanEligibility {

  LoanEligibilityResultDTO on(LoanEligibilityRequestDTO eligibilityRequest);
}
