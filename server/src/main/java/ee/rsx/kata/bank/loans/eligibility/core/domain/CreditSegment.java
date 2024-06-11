package ee.rsx.kata.bank.loans.eligibility.core.domain;

import ee.rsx.kata.bank.loans.validation.core.domain.SocialSecurityNumber;

import static ee.rsx.kata.bank.loans.eligibility.core.domain.CreditSegmentType.DEBT;

public record CreditSegment(
  SocialSecurityNumber ssn,
  CreditSegmentType type,
  int creditModifier
) {
 public int creditModifier() {
   return type == DEBT ? 0 : this.creditModifier;
 }
}
