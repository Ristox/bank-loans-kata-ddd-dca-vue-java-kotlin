package ee.rsx.kata.bank.loans.domain.limits.gateway;

import ee.rsx.kata.bank.loans.domain.segment.CreditSegment;

import java.util.Optional;

@FunctionalInterface
public interface DetermineEligiblePeriod {

  Optional<Integer> forLoan(Integer amount, CreditSegment creditSegment);
}
