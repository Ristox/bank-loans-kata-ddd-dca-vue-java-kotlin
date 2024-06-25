package ee.rsx.kata.bank.loans.adapter.eligibility.creditsegment

import ee.rsx.kata.bank.loans.domain.segment.CreditSegment
import ee.rsx.kata.bank.loans.domain.segment.CreditSegmentType
import ee.rsx.kata.bank.loans.domain.segment.CreditSegmentType.DEBT
import ee.rsx.kata.bank.loans.domain.segment.CreditSegmentType.SEGMENT_1
import ee.rsx.kata.bank.loans.domain.segment.CreditSegmentType.SEGMENT_2
import ee.rsx.kata.bank.loans.domain.segment.CreditSegmentType.SEGMENT_3
import ee.rsx.kata.bank.loans.domain.segment.gateway.FindCreditSegment
import ee.rsx.kata.bank.loans.domain.ssn.SocialSecurityNumber
import jakarta.inject.Named

@Named
internal class InMemoryCreditSegmentStorageAdapter : FindCreditSegment {

  override fun invoke(forPerson: SocialSecurityNumber) = creditSegmentOfPerson[forPerson.value]

  companion object {
    private val creditSegmentOfPerson = mapOf(
      "49002010965".let { it to segmentFor(it, DEBT, 666) },
      "49002010976".let { it to segmentFor(it, SEGMENT_1, 100) },
      "49002010987".let { it to segmentFor(it, SEGMENT_2, 300) },
      "49002010998".let { it to segmentFor(it, SEGMENT_3, 1000) }
    )

    private fun segmentFor(ssn: String, withType: CreditSegmentType, withCreditModifier: Int) =
      CreditSegment(SocialSecurityNumber(ssn), withType, withCreditModifier)
  }
}
