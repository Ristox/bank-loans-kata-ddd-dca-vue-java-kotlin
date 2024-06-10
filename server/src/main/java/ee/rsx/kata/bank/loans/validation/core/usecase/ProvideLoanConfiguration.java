package ee.rsx.kata.bank.loans.validation.core.usecase;

import ee.rsx.kata.bank.loans.validation.LoadValidationLimits;
import ee.rsx.kata.bank.loans.validation.ValidationLimitsDTO;
import ee.rsx.kata.bank.loans.validation.core.domain.LoanConfigGateway;
import ee.rsx.kata.bank.loans.validation.core.domain.LoanLimitsConfig;
import jakarta.inject.Named;
import lombok.RequiredArgsConstructor;

@Named
@RequiredArgsConstructor
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

  @Override
  public ValidationLimitsDTO invoke() {
    var limitsConfig = gateway.loadLimits();

    return toDto(limitsConfig);
  }
}
