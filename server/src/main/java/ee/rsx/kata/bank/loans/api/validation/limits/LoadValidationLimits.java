package ee.rsx.kata.bank.loans.api.validation.limits;

@FunctionalInterface
public interface LoadValidationLimits {

  ValidationLimitsDTO invoke();
}
