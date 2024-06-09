package ee.rsx.kata.bank.loans.validation;

@FunctionalInterface
public interface LoadValidationLimits {

  ValidationLimitsDTO invoke();
}
