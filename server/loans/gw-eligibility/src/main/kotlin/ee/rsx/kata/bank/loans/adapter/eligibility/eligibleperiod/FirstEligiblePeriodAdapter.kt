package ee.rsx.kata.bank.loans.adapter.eligibility.eligibleperiod

import ee.rsx.kata.bank.loans.domain.limits.gateway.DetermineEligiblePeriod
import ee.rsx.kata.bank.loans.domain.segment.CreditSegment
import jakarta.inject.Named
import kotlin.math.floor

@Named
internal class FirstEligiblePeriodAdapter : DetermineEligiblePeriod {

  override fun invoke(forAmount: Int, forSegment: CreditSegment): Int? =
    if (forSegment.isDebt)
      null
    else
      calculateFirstMinimumPeriodEligibleFor(forAmount, forSegment)

  private fun calculateFirstMinimumPeriodEligibleFor(amount: Int, creditSegment: CreditSegment): Int {
    val firstPeriod = floor(amount.toDouble() / creditSegment.creditModifier)
    return firstPeriod.toInt() + 1
  }
}
