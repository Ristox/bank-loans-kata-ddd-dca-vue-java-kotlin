package ee.rsx.kata.bank.loans.validation;

@FunctionalInterface
public interface ValidateSocialSecurityNumber {

  SsnValidationResultDTO on(String ssn);
}
