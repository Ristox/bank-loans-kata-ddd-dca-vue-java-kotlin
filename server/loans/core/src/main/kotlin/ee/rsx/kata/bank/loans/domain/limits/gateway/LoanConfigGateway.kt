package ee.rsx.kata.bank.loans.domain.limits.gateway;

import ee.rsx.kata.bank.loans.domain.limits.LoanLimitsConfig;

public interface LoanConfigGateway {

  LoanLimitsConfig loadLimits();
}
