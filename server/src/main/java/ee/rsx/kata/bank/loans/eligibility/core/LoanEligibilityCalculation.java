package ee.rsx.kata.bank.loans.eligibility.core;

import ee.rsx.kata.bank.loans.eligibility.CalculateLoanEligibility;
import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityRequestDTO;
import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityResultDTO;
import jakarta.inject.Named;

import static ee.rsx.kata.bank.loans.eligibility.LoanEligibilityStatus.APPROVED;

@Named
public class LoanEligibilityCalculation implements CalculateLoanEligibility {

  @Override
  public LoanEligibilityResultDTO on(LoanEligibilityRequestDTO eligibilityRequest) {
    return new LoanEligibilityResultDTO(
      APPROVED,
      eligibilityRequest.ssn(),
      eligibilityRequest.loanAmount(),
      eligibilityRequest.loanPeriodMonths()
    );
  }
}
