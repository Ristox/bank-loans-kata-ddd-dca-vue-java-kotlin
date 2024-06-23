package ee.rsx.kata.bank.loans.domain.limits.gateway

import ee.rsx.kata.bank.loans.domain.limits.LoanLimitsConfig

fun interface LoanConfigGateway {
  fun loadLimits(): LoanLimitsConfig
}
