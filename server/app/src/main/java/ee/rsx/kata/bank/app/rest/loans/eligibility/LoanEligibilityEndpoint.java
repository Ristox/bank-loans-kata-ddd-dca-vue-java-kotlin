package ee.rsx.kata.bank.app.rest.loans.eligibility;

import ee.rsx.kata.bank.loans.api.eligibility.CalculateLoanEligibility;
import ee.rsx.kata.bank.loans.api.eligibility.LoanEligibilityRequestDTO;
import ee.rsx.kata.bank.loans.api.eligibility.LoanEligibilityResultDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.ResponseEntity.status;

@RestController
@RequiredArgsConstructor
public class LoanEligibilityEndpoint {

  private final CalculateLoanEligibility calculateLoanEligibility;

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

