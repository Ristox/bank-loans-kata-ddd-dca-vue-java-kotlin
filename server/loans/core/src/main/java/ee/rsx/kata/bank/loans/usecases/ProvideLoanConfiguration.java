package ee.rsx.kata.bank.loans.usecases;

import ee.rsx.kata.bank.loans.validation.limits.LoadValidationLimits;
import ee.rsx.kata.bank.loans.validation.limits.ValidationLimitsDTO;
import ee.rsx.kata.bank.loans.domain.limits.LoanConfigGateway;
import ee.rsx.kata.bank.loans.domain.limits.LoanLimitsConfig;
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
