package ee.rsx.kata.bank.app.rest.loans.validation;

import ee.rsx.kata.bank.loans.validation.SsnValidationResultDTO;
import ee.rsx.kata.bank.loans.validation.ValidateSocialSecurityNumber;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ValidationEndpoint {

  private final ValidateSocialSecurityNumber validateSocialSecurityNumber;

  @GetMapping(value = "/loans/validation/ssn")
  public SsnValidationResultDTO validateSsn(@RequestParam("value") String value) {
    return validateSocialSecurityNumber.on(value);
  }
}

