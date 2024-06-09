package ee.rsx.kata.bank.loans.validation.core.domain;

public record LoanLimitsConfig(
  Integer minimumLoanAmount,
  Integer maximumLoanAmount,
  Integer minimumLoanPeriodMonths,
  Integer maximumLoanPeriodMonths
) {}
