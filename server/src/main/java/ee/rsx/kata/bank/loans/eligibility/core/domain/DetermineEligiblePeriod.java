package ee.rsx.kata.bank.loans.eligibility.core.domain;

import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityRequestDTO;
import jakarta.inject.Named;

import java.util.Optional;

@FunctionalInterface
public interface DetermineEligiblePeriod {

  Optional<Integer> forLoan(LoanEligibilityRequestDTO request, CreditSegment creditSegment);
}

@Named
class DummyEligiblePeriodReturningEmpty implements DetermineEligiblePeriod {

  @Override
  public Optional<Integer> forLoan(LoanEligibilityRequestDTO request, CreditSegment creditSegment) {
    return Optional.empty();
  }
}
