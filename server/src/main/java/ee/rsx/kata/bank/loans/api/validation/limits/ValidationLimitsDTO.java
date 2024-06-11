package ee.rsx.kata.bank.loans.api.validation.limits;

public record ValidationLimitsDTO(
  Integer minimumLoanAmount,
  Integer maximumLoanAmount,
  Integer minimumLoanPeriodMonths,
  Integer maximumLoanPeriodMonths
) {}
