package ee.rsx.kata.bank.loans.domain.limits.gateway

import ee.rsx.kata.bank.loans.domain.segment.CreditSegment
import java.util.*

fun interface DetermineEligiblePeriod {
  fun forLoan(amount: Int, creditSegment: CreditSegment): Optional<Int>
}
