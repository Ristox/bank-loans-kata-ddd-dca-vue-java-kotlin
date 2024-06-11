package ee.rsx.kata.bank.loans.eligibility;

public record LoanEligibilityRequestDTO(
  String ssn,
  Integer loanAmount,
  Integer loanPeriodMonths
) {}
