package ee.rsx.kata.bank.loans.api.eligibility;

@FunctionalInterface
public interface CalculateLoanEligibility {

  LoanEligibilityResultDTO on(LoanEligibilityRequestDTO eligibilityRequest);
}
