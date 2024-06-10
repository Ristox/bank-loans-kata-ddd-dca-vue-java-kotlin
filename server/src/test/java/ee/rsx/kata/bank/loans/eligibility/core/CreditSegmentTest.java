package ee.rsx.kata.bank.loans.eligibility.core;

import ee.rsx.kata.bank.loans.validation.core.domain.SocialSecurityNumber;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;

@DisplayName("Credit Segment")
class CreditSegmentTest {

  private static final SocialSecurityNumber SSN = new SocialSecurityNumber("49002010998");

  @ParameterizedTest
  @EnumSource(value = CreditSegmentType.class, mode = EXCLUDE, names = { "DEBT" })
  @DisplayName("has originally assigned credit modifier, when it's a non-debt segment of")
  void hasOriginallyAssignedCreditModifier_when_itsNonDebtSegmentOf(CreditSegmentType type) {
    int assignedCreditModifier = 500;

    assertThat(new CreditSegment(SSN, type, assignedCreditModifier))
      .extracting(CreditSegment::creditModifier)
      .isEqualTo(assignedCreditModifier);
  }

  @ParameterizedTest
  @EnumSource(value = CreditSegmentType.class, names = { "DEBT" })
  @DisplayName("has a credit modifier of 0, when it's a debt segment as")
  void hasCreditModifierOfZero_when_itsDebtSegmentAs(CreditSegmentType debtType) {
    int assignedCreditModifier = 500;

    assertThat(new CreditSegment(SSN, debtType, assignedCreditModifier))
      .extracting(CreditSegment::creditModifier)
      .isEqualTo(0);
  }
}
