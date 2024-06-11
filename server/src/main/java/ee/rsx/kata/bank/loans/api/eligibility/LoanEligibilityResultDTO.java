package ee.rsx.kata.bank.loans.api.eligibility;

import java.util.List;

public record LoanEligibilityResultDTO(
  LoanEligibilityStatus result,
  List<String> errors,
  String ssn,
  Integer loanAmount,
  Integer loanPeriodMonths,
  Integer eligibleLoanAmount,
  Integer eligibleLoanPeriod
) {}
