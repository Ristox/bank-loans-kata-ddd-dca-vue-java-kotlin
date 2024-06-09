package ee.rsx.kata.bank.loans.validation;

public record ValidationLimitsDTO(
  Integer minimumLoanAmount,
  Integer maximumLoanAmount,
  Integer minimumLoanPeriodMonths,
  Integer maximumLoanPeriodMonths
) {}
