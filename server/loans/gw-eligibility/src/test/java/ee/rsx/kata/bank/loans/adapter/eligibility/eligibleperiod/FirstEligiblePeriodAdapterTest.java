package ee.rsx.kata.bank.loans.adapter.eligibility.eligibleperiod;

import ee.rsx.kata.bank.loans.domain.segment.CreditSegment;
import ee.rsx.kata.bank.loans.domain.ssn.SocialSecurityNumber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static ee.rsx.kata.bank.loans.domain.segment.CreditSegmentType.*;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Eligible period calculation adapter, which returns first period that would be eligible for given amount and credit modifier")
class FirstEligiblePeriodAdapterTest {

  private FirstEligiblePeriodAdapter determineEligiblePeriod;

  @BeforeEach
  void setup() {
    determineEligiblePeriod = new FirstEligiblePeriodAdapter();
  }

  @Test
  @DisplayName("returns the first eligible period of 51 months, based on the given loan amount and credit modifier")
  void returns_firstEligiblePeriod_of_51_Months_basedOn_givenLoanAmountAndCreditModifier() {
    var requestedLoan = 5000;
    var creditModifier = 100;
    var segment = new CreditSegment(new SocialSecurityNumber("49002010976"), SEGMENT_1, creditModifier);

    Optional<Integer> eligiblePeriod = determineEligiblePeriod.forLoan(requestedLoan, segment);

    assertThat(eligiblePeriod)
      .isPresent()
      .contains(51);
  }

  @Test
  @DisplayName(("returns no eligible period, when given credit segment is a debt segment"))
  void returns_noEligiblePeriod_whenGivenCreditSegmentIsADebtSegment() {
    var requestedLoan = 5000;
    var creditModifier = 100;
    var debtSegment = new CreditSegment(new SocialSecurityNumber("49002010976"), DEBT, creditModifier);

    Optional<Integer> eligiblePeriod = determineEligiblePeriod.forLoan(requestedLoan, debtSegment);

    assertThat(eligiblePeriod)
      .isNotPresent();
  }
}
