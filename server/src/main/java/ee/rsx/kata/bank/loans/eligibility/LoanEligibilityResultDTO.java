package ee.rsx.kata.bank.loans.eligibility;

import java.util.List;

public record LoanEligibilityResultDTO(
  LoanEligibilityStatus result,
  List<String> errors,
  String ssn,
  Integer loanAmount,
  Integer loanPeriodMonths
) {}
