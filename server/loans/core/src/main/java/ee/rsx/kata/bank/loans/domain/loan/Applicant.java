package ee.rsx.kata.bank.loans.domain.loan;

import ee.rsx.kata.bank.loans.domain.segment.CreditSegment;
import ee.rsx.kata.bank.loans.domain.ssn.SocialSecurityNumber;
import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityStatus;

import java.util.Optional;

import static ee.rsx.kata.bank.loans.eligibility.LoanEligibilityStatus.*;
import static java.util.Objects.*;
import static java.util.Optional.*;

public record Applicant(
  SocialSecurityNumber ssn,
  CreditSegment segment,
  Loan loan
) {

  public static Applicant with(SocialSecurityNumber ssn, CreditSegment segment, Loan loan) {
    return new Applicant(ssn, segment, loan);
  }

  public Applicant {
    if (isNull(ssn)) {
      throw new IllegalArgumentException("Applicant SSN must be provided");
    }
    if (isNull(segment)) {
      throw new IllegalArgumentException("Applicant Credit Segment must be provided");
    }
    if (isNull(loan)) {
      throw new IllegalArgumentException("Applicant Loan must be provided");
    }
  }

  public boolean isNotInDebt() {
    return !segment.isDebt();
  }

  public LoanEligibility eligibilityForNewPeriod(Integer newPeriod) {
    return eligibleAmountWithin(newPeriod)
      .map(amount -> new LoanEligibility(eligibilityStatus(), amount, newPeriod))
      .orElseGet(() -> new LoanEligibility(eligibilityStatus(), null, newPeriod));
  }

  public LoanEligibility eligibility() {
    return attemptFindingEligibilityWithAmount().orElseGet(() -> new LoanEligibility(DENIED, null, null));
  }

  public Optional<LoanEligibility> attemptFindingEligibilityWithAmount() {
    return eligibleAmount().map(amount -> new LoanEligibility(eligibilityStatus(), amount, null));
  }

  private Optional<Integer> eligibleAmount() {
    return eligibleAmountWithin(loan.periodInMonths());
  }

  private Optional<Integer> eligibleAmountWithin(Integer loanPeriodInMonths) {
    int eligibleAmount = Math.min(loan.maximumAmount(), maximumCreditFor(loanPeriodInMonths));

    return eligibleAmount >= loan.minimumAmount() ? of(eligibleAmount) : empty();
  }

  private LoanEligibilityStatus eligibilityStatus() {
    return isNotInDebt() && creditScore() > 1 ? APPROVED : DENIED;
  }

  private int maximumCreditFor(Integer loanPeriodInMonths) {
    return segment.creditModifier() * loanPeriodInMonths - 1;
  }

  private double creditScore() {
    return (double) segment.creditModifier() / loan.amount() * loan.periodInMonths();
  }
}
