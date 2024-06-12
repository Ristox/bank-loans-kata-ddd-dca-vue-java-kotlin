package ee.rsx.kata.bank.loans.domain.limits.gateway;

import ee.rsx.kata.bank.loans.domain.segment.CreditSegment;
import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityRequestDTO;

import java.util.Optional;

@FunctionalInterface
public interface DetermineEligiblePeriod {

  Optional<Integer> forLoan(LoanEligibilityRequestDTO request, CreditSegment creditSegment);
}
