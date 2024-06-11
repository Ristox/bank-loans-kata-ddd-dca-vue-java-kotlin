package ee.rsx.kata.bank.loans.usecases;

import ee.rsx.kata.bank.loans.api.validation.ssn.SsnValidationResultDTO;
import ee.rsx.kata.bank.loans.api.validation.ssn.ValidateSocialSecurityNumber;
import ee.rsx.kata.bank.loans.domain.ssn.SocialSecurityNumber;
import jakarta.inject.Named;

import java.time.format.DateTimeParseException;

import static ee.rsx.kata.bank.loans.api.validation.ssn.SsnValidationResultDTO.*;

@Named
class SsnValidation implements ValidateSocialSecurityNumber {

  @Override
  public SsnValidationResultDTO on(String ssn) {
    try {
      var validSsn = new SocialSecurityNumber(ssn);
      return okResultWith(validSsn.value());
    } catch (IllegalArgumentException | IllegalStateException | DateTimeParseException e) {
      return invalidResultWith(ssn);
    }
  }
}
