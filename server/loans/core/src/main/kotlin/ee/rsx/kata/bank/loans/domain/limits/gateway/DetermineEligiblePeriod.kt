package ee.rsx.kata.bank.loans.domain.limits.gateway

import ee.rsx.kata.bank.loans.domain.segment.CreditSegment

fun interface DetermineEligiblePeriod {
  operator fun invoke(forAmount: Int, forSegment: CreditSegment): Int?
}
