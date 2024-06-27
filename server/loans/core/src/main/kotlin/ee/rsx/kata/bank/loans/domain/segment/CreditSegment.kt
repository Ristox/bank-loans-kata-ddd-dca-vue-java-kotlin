package ee.rsx.kata.bank.loans.domain.segment

import ee.rsx.kata.bank.loans.domain.segment.CreditSegmentType.DEBT
import ee.rsx.kata.bank.loans.domain.ssn.SocialSecurityNumber

data class CreditSegment(
  val ssn: SocialSecurityNumber,
  val type: CreditSegmentType,
  private val modifier: Int
) {
  val isDebt
    get() = type === DEBT

  val creditModifier
    get() = if (isDebt) 0 else modifier
}
