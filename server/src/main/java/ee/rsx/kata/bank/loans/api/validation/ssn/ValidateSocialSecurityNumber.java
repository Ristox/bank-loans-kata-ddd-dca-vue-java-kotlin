package ee.rsx.kata.bank.loans.api.validation.ssn;

@FunctionalInterface
public interface ValidateSocialSecurityNumber {

  SsnValidationResultDTO on(String ssn);
}
