package ee.rsx.kata.bank.loans.validation.core.domain;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.time.LocalDate.now;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.regex.Pattern.compile;

public record SocialSecurityNumber(String value) {

  private static final Pattern SSN_PATTERN = compile("^[0-9]\\d{2}[0-1]\\d{7}$");
  private static final DateTimeFormatter SSN_DATE_FORMAT = ofPattern("yyyyMMdd");

  /**
   * @throws IllegalArgumentException when provided SSN value is invalid (does not correspond to Estonian SSN rules)
   * @throws IllegalStateException when century prefix of provided SSN value is invalid (not between 1...6)
   * @throws DateTimeParseException when birth date part in of provided SSN does not represent a valid date
   */
  public SocialSecurityNumber(String value) {
    this.value = value;
    validate();
  }

  private void validate() {
    boolean isValid =
      SSN_PATTERN.matcher(value).matches() &&
      isValidBirthDate() &&
      calculateChecksum() == parseChecksumAtTheEnd();

    if (!isValid) {
      throw new IllegalArgumentException();
    }
  }

  private boolean isValidBirthDate() {
    LocalDate birthDate = parseBirthDate();
    return birthDate.isEqual(now()) || birthDate.isBefore(now());
  }

  private LocalDate parseBirthDate() {
    var date = value.substring(1, 7);
    String century = value.substring(0, 1);
    date = switch (century) {
      case "1", "2" -> "18" + date;
      case "3", "4" -> "19" + date;
      case "5", "6" -> "20" + date;
      default -> throw new IllegalStateException(
        format("Illegal century value (%s) in social security number (%s)", century, value)
      );
    };
    return LocalDate.parse(date, SSN_DATE_FORMAT);
  }

  private int calculateChecksum() {
    int[] multipliers = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 1};

    int total = totalOfEachSsnNumberMultipliedWith(multipliers);
    var modulus = total % 11;
    if (isDoubleDigit(modulus)) {
      modulus = recalculateChecksumSpecialCase();
    }
    return modulus;
  }


  private int recalculateChecksumSpecialCase() {
    int[] multipliers = new int[]{3, 4, 5, 6, 7, 8, 9, 1, 2, 3};

    int total = totalOfEachSsnNumberMultipliedWith(multipliers);
    var modulus = total % 11;
    if (isDoubleDigit(modulus)) {
      modulus = 0;
    }

    return modulus;
  }

  private int totalOfEachSsnNumberMultipliedWith(int[] multipliers) {
    AtomicInteger total = new AtomicInteger();

    IntStream
      .range(0, value.length() - 1)
      .forEach((index) -> total.addAndGet(numberAt(index) * multipliers[index]));

    return total.get();
  }

  private int numberAt(int index) {
    return parseInt(value.substring(index, index + 1));
  }

  private boolean isDoubleDigit(int modulus) {
    return 10 == modulus;
  }

  private int parseChecksumAtTheEnd() {
    return parseInt(value.substring(value.length() - 1));
  }
}
