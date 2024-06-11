package ee.rsx.kata.bank.loans.domain.limits;

public record LoanLimitsConfig(
  Integer minimumLoanAmount,
  Integer maximumLoanAmount,
  Integer minimumLoanPeriodMonths,
  Integer maximumLoanPeriodMonths
) {}
