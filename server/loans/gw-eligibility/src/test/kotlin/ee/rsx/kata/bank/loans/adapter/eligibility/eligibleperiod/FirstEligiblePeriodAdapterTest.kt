package ee.rsx.kata.bank.loans.adapter.eligibility.eligibleperiod

import ee.rsx.kata.bank.loans.domain.segment.CreditSegment
import ee.rsx.kata.bank.loans.domain.segment.CreditSegmentType.DEBT
import ee.rsx.kata.bank.loans.domain.segment.CreditSegmentType.SEGMENT_1
import ee.rsx.kata.bank.loans.domain.ssn.SocialSecurityNumber
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Eligible period calculation adapter, which returns first period that would be eligible for given amount and credit modifier")
internal class FirstEligiblePeriodAdapterTest {

  private lateinit var determineEligiblePeriod: FirstEligiblePeriodAdapter

  @BeforeEach
  fun setup() {
    determineEligiblePeriod = FirstEligiblePeriodAdapter()
  }

  @Test
  fun `returns the first eligible period of 51 months, based on the given loan amount and credit modifier`() {
    val requestedLoan = 5000
    val creditModifier = 100
    val segment = CreditSegment(SocialSecurityNumber("49002010976"), SEGMENT_1, creditModifier)

    val eligiblePeriod = determineEligiblePeriod(forAmount = requestedLoan, forSegment = segment)

    assertThat(eligiblePeriod).isEqualTo(51)
  }

  @Test
  fun `returns no eligible period, when given credit segment is a debt segment`() {
    val requestedLoan = 5000
    val creditModifier = 100
    val debtSegment = CreditSegment(SocialSecurityNumber("49002010976"), DEBT, creditModifier)

    val eligiblePeriod = determineEligiblePeriod(forAmount = requestedLoan, forSegment = debtSegment)

    assertThat(eligiblePeriod).isNull()
  }
}
