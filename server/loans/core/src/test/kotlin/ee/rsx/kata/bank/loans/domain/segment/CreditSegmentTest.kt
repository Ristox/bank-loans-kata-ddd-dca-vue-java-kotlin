package ee.rsx.kata.bank.loans.domain.segment

import ee.rsx.kata.bank.loans.domain.ssn.SocialSecurityNumber
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE

@DisplayName("Credit Segment")
internal class CreditSegmentTest {

  @ParameterizedTest
  @EnumSource(value = CreditSegmentType::class, mode = EXCLUDE, names = ["DEBT"])
  fun `has originally assigned credit modifier, when it's a non-debt segment of`(type: CreditSegmentType) {
    val assignedCreditModifier = 500

    assertThat(CreditSegment(SSN, type, assignedCreditModifier))
      .extracting { it.creditModifier }
      .isEqualTo(assignedCreditModifier)
  }

  @ParameterizedTest
  @EnumSource(value = CreditSegmentType::class, names = ["DEBT"])
  fun `has credit modifier of 0, when it's a debt segment as`(debtType: CreditSegmentType) {
    val assignedCreditModifier = 500

    val result = CreditSegment(SSN, debtType, assignedCreditModifier)
    
    assertThat(result)
      .extracting { it.creditModifier }
      .isEqualTo(0)
  }

  companion object {
    private val SSN = SocialSecurityNumber("49002010998")
  }
}
