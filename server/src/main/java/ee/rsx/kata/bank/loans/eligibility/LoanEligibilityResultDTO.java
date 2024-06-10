package ee.rsx.kata.bank.loans.eligibility;

public record LoanEligibilityResultDTO(
  LoanEligibilityStatus result,
  String ssn,
  Integer loanAmount,
  Integer loanPeriodMonths
) {}
