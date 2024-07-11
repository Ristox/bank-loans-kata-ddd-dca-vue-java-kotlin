package ee.rsx.kata.bank.loans.domain.loan;

public record Loan(
  Integer amount,
  Integer periodInMonths,
  Integer minimumAmount,
  Integer maximumAmount
) {

  public static Loan with(Integer amount, Integer periodInMonths, Integer minimum, Integer maximum) {
    return new Loan(amount, periodInMonths, minimum, maximum);
  }

  public Loan {
    if (amount <= 0 ) {
      throw new IllegalArgumentException("Loan amount must be positive");
    }
    if (periodInMonths <= 0) {
      throw new IllegalArgumentException("Loan period must be positive");
    }
  }
}
