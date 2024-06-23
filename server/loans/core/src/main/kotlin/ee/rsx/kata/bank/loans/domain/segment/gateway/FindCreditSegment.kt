package ee.rsx.kata.bank.loans.domain.segment.gateway

import ee.rsx.kata.bank.loans.domain.segment.CreditSegment
import ee.rsx.kata.bank.loans.domain.ssn.SocialSecurityNumber
import java.util.*

fun interface FindCreditSegment {
  fun forPerson(ssn: SocialSecurityNumber): Optional<CreditSegment>
}
