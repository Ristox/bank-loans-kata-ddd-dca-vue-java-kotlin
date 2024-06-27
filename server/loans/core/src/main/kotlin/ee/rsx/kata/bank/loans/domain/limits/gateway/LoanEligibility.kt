package ee.rsx.kata.bank.loans.domain.limits.gateway

import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityStatus

data class LoanEligibility(
  val status: LoanEligibilityStatus,
  val eligibleAmount: Int? = null,
  val eligiblePeriod: Int? = null
)
