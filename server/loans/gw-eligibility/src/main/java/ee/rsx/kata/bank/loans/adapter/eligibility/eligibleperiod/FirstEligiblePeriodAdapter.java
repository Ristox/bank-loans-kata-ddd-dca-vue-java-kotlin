package ee.rsx.kata.bank.loans.adapter.eligibility.eligibleperiod;

import ee.rsx.kata.bank.loans.api.eligibility.LoanEligibilityRequestDTO;
import ee.rsx.kata.bank.loans.domain.DetermineEligiblePeriod;
import ee.rsx.kata.bank.loans.domain.segment.CreditSegment;
import jakarta.inject.Named;

import java.util.Optional;

import static ee.rsx.kata.bank.loans.domain.segment.CreditSegmentType.DEBT;
import static java.util.Optional.*;

@Named
public class FirstEligiblePeriodAdapter implements DetermineEligiblePeriod {

  @Override
  public Optional<Integer> forLoan(LoanEligibilityRequestDTO request, CreditSegment creditSegment) {
    if (creditSegment.type() == DEBT) {
      return empty();
    }
    return of(calculateFirstMinimumPeriodEligibleFor(creditSegment, request));
  }

  private Integer calculateFirstMinimumPeriodEligibleFor(CreditSegment creditSegment, LoanEligibilityRequestDTO request) {
    double firstPeriod = Math.floor((double) request.loanAmount() / creditSegment.creditModifier());

    return Double.valueOf(firstPeriod).intValue() + 1;
  }
}
