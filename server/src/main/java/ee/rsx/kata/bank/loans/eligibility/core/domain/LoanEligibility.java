package ee.rsx.kata.bank.loans.eligibility.core.domain;

import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityStatus;

public record LoanEligibility(
  LoanEligibilityStatus status,
  Integer eligibleAmount
) {}
