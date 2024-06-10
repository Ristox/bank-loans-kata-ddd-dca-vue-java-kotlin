package ee.rsx.kata.bank.app.rest.loans.eligibility;

import ee.rsx.kata.bank.loans.eligibility.CalculateLoanEligibility;
import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityRequestDTO;
import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityResultDTO;
import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoanEligibilityEndpoint {

  private final CalculateLoanEligibility calculateLoanEligibility;

  public LoanEligibilityEndpoint(CalculateLoanEligibility calculateLoanEligibility) {
    this.calculateLoanEligibility = calculateLoanEligibility;
  }

  @PostMapping(value = "/loans/eligibility")
  public LoanEligibilityResultDTO calculateLoanEligibility(
    @RequestBody LoanEligibilityRequestDTO request
  ) {
    return calculateLoanEligibility.on(request);
  }
}

