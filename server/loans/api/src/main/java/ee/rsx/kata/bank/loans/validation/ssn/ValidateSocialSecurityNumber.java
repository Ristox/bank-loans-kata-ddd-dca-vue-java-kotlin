package ee.rsx.kata.bank.loans.validation.ssn;

@FunctionalInterface
public interface ValidateSocialSecurityNumber {

  SsnValidationResultDTO on(String ssn);
}
