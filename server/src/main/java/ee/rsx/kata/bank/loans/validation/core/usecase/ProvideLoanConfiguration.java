package ee.rsx.kata.bank.loans.validation.core.usecase;

import ee.rsx.kata.bank.loans.validation.LoadValidationLimits;
import ee.rsx.kata.bank.loans.validation.ValidationLimitsDTO;
import ee.rsx.kata.bank.loans.validation.core.domain.LoanConfigGateway;
import ee.rsx.kata.bank.loans.validation.core.domain.LoanLimitsConfig;
import jakarta.inject.Named;

@Named
class ProvideLoanConfiguration implements LoadValidationLimits {

  private static ValidationLimitsDTO toDto(LoanLimitsConfig config) {
    return new ValidationLimitsDTO(
      config.minimumLoanAmount(),
      config.maximumLoanAmount(),
      config.minimumLoanPeriodMonths(),
      config.maximumLoanPeriodMonths()
    );
  }

  private final LoanConfigGateway gateway;

  ProvideLoanConfiguration(LoanConfigGateway gateway) {
    this.gateway = gateway;
  }

  @Override
  public ValidationLimitsDTO invoke() {
    LoanLimitsConfig limitsConfig = gateway.loadLimits();

    return toDto(limitsConfig);
  }
}
