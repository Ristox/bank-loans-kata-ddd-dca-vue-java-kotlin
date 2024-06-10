package ee.rsx.kata.bank.loans.validation.core.usecase;

import ee.rsx.kata.bank.loans.validation.SsnValidationResultDTO;
import ee.rsx.kata.bank.loans.validation.ValidateSocialSecurityNumber;
import ee.rsx.kata.bank.loans.validation.core.domain.SocialSecurityNumber;
import jakarta.inject.Named;

import java.time.format.DateTimeParseException;

import static ee.rsx.kata.bank.loans.validation.SsnValidationResultDTO.*;

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
