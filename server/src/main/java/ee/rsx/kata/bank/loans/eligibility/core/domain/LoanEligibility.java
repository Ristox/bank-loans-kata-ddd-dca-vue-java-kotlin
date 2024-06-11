package ee.rsx.kata.bank.loans.eligibility.core.domain;

import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityStatus;
import jakarta.annotation.Nullable;

public record LoanEligibility(
  LoanEligibilityStatus status,
  @Nullable Integer eligibleAmount,
  @Nullable Integer eligiblePeriod
) {}
