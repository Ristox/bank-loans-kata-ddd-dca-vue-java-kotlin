package ee.rsx.kata.bank.loans.adapter.eligibility.eligibleperiod;

import ee.rsx.kata.bank.loans.domain.limits.gateway.DetermineEligiblePeriod;
import ee.rsx.kata.bank.loans.domain.segment.CreditSegment;
import jakarta.inject.Named;

import java.util.Optional;

import static java.util.Optional.*;

@Named
class FirstEligiblePeriodAdapter implements DetermineEligiblePeriod {

  @Override
  public Optional<Integer> forLoan(Integer amount, CreditSegment creditSegment) {
    if (creditSegment.isDebt()) {
      return empty();
    }
    return of(calculateFirstMinimumPeriodEligibleFor(amount, creditSegment));
  }

  private Integer calculateFirstMinimumPeriodEligibleFor(Integer amount, CreditSegment creditSegment) {
    double firstPeriod = Math.floor((double) amount / creditSegment.creditModifier());

    return Double.valueOf(firstPeriod).intValue() + 1;
  }
}
