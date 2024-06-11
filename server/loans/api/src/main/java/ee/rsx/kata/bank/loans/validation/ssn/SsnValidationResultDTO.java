package ee.rsx.kata.bank.loans.validation.ssn;

import static ee.rsx.kata.bank.loans.validation.ssn.ValidationStatus.INVALID;

public record SsnValidationResultDTO(String ssn, ValidationStatus status) {

  public static SsnValidationResultDTO okResultWith(String ssn) {
    return new SsnValidationResultDTO(ssn, ValidationStatus.OK);
  }

  public static SsnValidationResultDTO invalidResultWith(String ssn) {
    return new SsnValidationResultDTO(ssn, INVALID);
  }
}
