package ee.rsx.kata.bank.loans.domain;

import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityRequestDTO;
import ee.rsx.kata.bank.loans.domain.segment.CreditSegment;

import java.util.Optional;

@FunctionalInterface
public interface DetermineEligiblePeriod {

  Optional<Integer> forLoan(LoanEligibilityRequestDTO request, CreditSegment creditSegment);
}
