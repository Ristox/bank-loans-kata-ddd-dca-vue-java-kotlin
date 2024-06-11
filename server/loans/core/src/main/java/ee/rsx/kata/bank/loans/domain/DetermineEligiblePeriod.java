package ee.rsx.kata.bank.loans.domain;

import ee.rsx.kata.bank.loans.api.eligibility.LoanEligibilityRequestDTO;
import ee.rsx.kata.bank.loans.domain.segment.CreditSegment;

import java.util.Optional;

@FunctionalInterface
public interface DetermineEligiblePeriod {

  Optional<Integer> forLoan(LoanEligibilityRequestDTO request, CreditSegment creditSegment);
}
