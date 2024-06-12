package ee.rsx.kata.bank.app.rest.loans.validation;

import ee.rsx.kata.bank.loans.validation.limits.LoadValidationLimits;
import ee.rsx.kata.bank.loans.validation.limits.ValidationLimitsDTO;
import ee.rsx.kata.bank.loans.validation.ssn.SsnValidationResultDTO;
import ee.rsx.kata.bank.loans.validation.ssn.ValidateSocialSecurityNumber;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/loans/validation")
@RequiredArgsConstructor
public class ValidationEndpoints {

  private final LoadValidationLimits loadValidationLimits;
  private final ValidateSocialSecurityNumber validateSocialSecurityNumber;

  @GetMapping(value = "/limits")
  public ValidationLimitsDTO loadValidationLimits() {
    return loadValidationLimits.invoke();
  }

  @GetMapping(value = "/ssn")
  public SsnValidationResultDTO validateSsn(@RequestParam("value") String value) {
    return validateSocialSecurityNumber.on(value);
  }
}

