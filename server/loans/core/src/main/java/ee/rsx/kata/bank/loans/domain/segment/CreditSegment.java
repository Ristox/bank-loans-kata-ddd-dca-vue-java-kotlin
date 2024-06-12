package ee.rsx.kata.bank.loans.domain.segment;

import ee.rsx.kata.bank.loans.domain.ssn.SocialSecurityNumber;

import static ee.rsx.kata.bank.loans.domain.segment.CreditSegmentType.DEBT;

public record CreditSegment(
  SocialSecurityNumber ssn,
  CreditSegmentType type,
  int creditModifier
) {
 public int creditModifier() {

   return type == DEBT ? 0 : this.creditModifier;
 }
 public boolean isDebt() {
   return type == DEBT;
 }
}
