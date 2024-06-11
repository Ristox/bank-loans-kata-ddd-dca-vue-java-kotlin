package ee.rsx.kata.bank.loans.eligibility.gateway.adapter.eligibleperiod;

import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityRequestDTO;
import ee.rsx.kata.bank.loans.eligibility.core.domain.CreditSegment;
import ee.rsx.kata.bank.loans.validation.core.domain.SocialSecurityNumber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static ee.rsx.kata.bank.loans.eligibility.core.domain.CreditSegmentType.*;
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
    var request = new LoanEligibilityRequestDTO("49002010976", requestedLoan, 12);
    var segment = new CreditSegment(new SocialSecurityNumber("49002010976"), SEGMENT_1, creditModifier);

    Optional<Integer> eligiblePeriod = determineEligiblePeriod.forLoan(request, segment);

    assertThat(eligiblePeriod)
      .isPresent()
      .contains(51);
  }

  @Test
  @DisplayName(("returns no eligible period, when given credit segment is a debt segment"))
  void returns_noEligiblePeriod_whenGivenCreditSegmentIsADebtSegment() {
    var requestedLoan = 5000;
    var creditModifier = 100;
    var debtSegment = DEBT;
    var request = new LoanEligibilityRequestDTO("49002010976", requestedLoan, 12);
    var segment = new CreditSegment(new SocialSecurityNumber("49002010976"), debtSegment, creditModifier);

    Optional<Integer> eligiblePeriod = determineEligiblePeriod.forLoan(request, segment);

    assertThat(eligiblePeriod)
      .isNotPresent();
  }

}
