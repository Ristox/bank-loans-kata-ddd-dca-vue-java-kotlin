package ee.rsx.kata.bank.loans.adapter.validation;

import ee.rsx.kata.bank.loans.domain.limits.LoanConfigGateway;
import ee.rsx.kata.bank.loans.domain.limits.LoanLimitsConfig;
import jakarta.inject.Named;

@Named
class LoanConfigurationAdapter implements LoanConfigGateway {

  private static final int MINIMUM_LOAN_AMOUNT = 2_000;
  private static final int MAXIMUM_LOAN_AMOUNT = 10_000;
  private static final int MINIMUM_LOAN_PERIOD_MONTHS = 12;
  private static final int MAXIMUM_LOAN_PERIOD_MONTHS = 60;

  @Override
  public LoanLimitsConfig loadLimits() {

    return new LoanLimitsConfig(
      MINIMUM_LOAN_AMOUNT,
      MAXIMUM_LOAN_AMOUNT,
      MINIMUM_LOAN_PERIOD_MONTHS,
      MAXIMUM_LOAN_PERIOD_MONTHS
    );
  }
}
