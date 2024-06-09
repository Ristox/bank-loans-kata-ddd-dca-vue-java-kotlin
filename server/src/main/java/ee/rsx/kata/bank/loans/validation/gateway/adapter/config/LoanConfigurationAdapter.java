package ee.rsx.kata.bank.loans.validation.gateway.adapter.config;

import ee.rsx.kata.bank.loans.validation.core.domain.LoanConfigGateway;
import ee.rsx.kata.bank.loans.validation.core.domain.LoanLimitsConfig;
import jakarta.inject.Named;

@Named
public class LoanConfigurationAdapter implements LoanConfigGateway {

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
