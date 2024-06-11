package ee.rsx.kata.bank.loans.validation.limits;

@FunctionalInterface
public interface LoadValidationLimits {

  ValidationLimitsDTO invoke();
}
