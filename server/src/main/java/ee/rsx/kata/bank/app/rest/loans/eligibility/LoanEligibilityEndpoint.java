package ee.rsx.kata.bank.app.rest.loans.eligibility;

import ee.rsx.kata.bank.loans.eligibility.CalculateLoanEligibility;
import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityRequestDTO;
import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityResultDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.ResponseEntity.status;

@RestController
public class LoanEligibilityEndpoint {

  private final CalculateLoanEligibility calculateLoanEligibility;

  public LoanEligibilityEndpoint(CalculateLoanEligibility calculateLoanEligibility) {
    this.calculateLoanEligibility = calculateLoanEligibility;
  }

  @PostMapping(value = "/loans/eligibility")
  public ResponseEntity<LoanEligibilityResultDTO> calculateLoanEligibility(
    @RequestBody LoanEligibilityRequestDTO request
  ) {
    var eligibility = calculateLoanEligibility.on(request);

    var httpStatus = switch (eligibility.result()) {
      case APPROVED -> OK;
      case INVALID -> BAD_REQUEST;
      case DENIED -> NOT_ACCEPTABLE;
    };

    return status(httpStatus)
      .contentType(APPLICATION_JSON)
      .body(eligibility);
  }
}


