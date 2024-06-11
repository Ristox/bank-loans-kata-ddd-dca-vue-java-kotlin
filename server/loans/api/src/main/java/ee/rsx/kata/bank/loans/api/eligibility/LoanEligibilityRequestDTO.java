package ee.rsx.kata.bank.loans.api.eligibility;

public record LoanEligibilityRequestDTO(
  String ssn,
  Integer loanAmount,
  Integer loanPeriodMonths
) {}
