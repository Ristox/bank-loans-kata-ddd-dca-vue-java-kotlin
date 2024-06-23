package ee.rsx.kata.bank.loans.adapter.eligibility.eligibleperiod

import ee.rsx.kata.bank.loans.domain.limits.gateway.DetermineEligiblePeriod
import ee.rsx.kata.bank.loans.domain.segment.CreditSegment
import jakarta.inject.Named
import java.util.*
import java.util.Optional.empty
import java.util.Optional.of
import kotlin.math.floor

@Named
internal class FirstEligiblePeriodAdapter : DetermineEligiblePeriod {

  override fun forLoan(amount: Int, creditSegment: CreditSegment): Optional<Int> =
    if (creditSegment.isDebt)
      empty()
    else
      of(calculateFirstMinimumPeriodEligibleFor(amount, creditSegment))

  private fun calculateFirstMinimumPeriodEligibleFor(amount: Int, creditSegment: CreditSegment): Int {
    val firstPeriod = floor(amount.toDouble() / creditSegment.creditModifier)
    return firstPeriod.toInt() + 1
  }
}
