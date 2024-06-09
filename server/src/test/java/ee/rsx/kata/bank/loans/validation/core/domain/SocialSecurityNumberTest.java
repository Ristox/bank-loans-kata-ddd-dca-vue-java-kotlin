package ee.rsx.kata.bank.loans.validation.core.domain;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.format.DateTimeParseException;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Social Security Number")
class SocialSecurityNumberTest {

  private static Stream<Arguments> validSocialSecurityNumbers() {
    return Stream.of(
      ssnArg("46301055224"),
      ssnArg("37205250269"),
      ssnArg("50212104262"),
      ssnArg("34110140248"),
      ssnArg("49002010976"),
      ssnArg("49002010965"),
      ssnArg("49002010998"),
      ssnArg("49002010987"),
      ssnArg("61504293707")
    );
  }

  private static Arguments ssnArg(String ssn) {
    return Arguments.of(ssn);
  }

  @ParameterizedTest
  @MethodSource("validSocialSecurityNumbers")
  @DisplayName("Is created, when constructed with a valid ssn value of")
  void isCreated_when_constructed_withValid(String ssnValue) {
    assertDoesNotThrow(
      () -> new SocialSecurityNumber(ssnValue)
    );
  }

  @Test
  @DisplayName("Has value equal to the valid provided ssn value")
  void hasValue_equalTo_validProvidedSsnValue() {
    String providedSsnValue = "49002010998";

    SocialSecurityNumber validSsn = new SocialSecurityNumber(providedSsnValue);

    assertThat(validSsn.value())
      .isEqualTo(providedSsnValue);
  }

  @Nested
  @DisplayName("Creation fails, when SSN value")
  class CreationFailsWhenSsnValue {

    private static Stream<Arguments> invalidDaysOfMonth() {
      return Stream.of(
        Arguments.of("00"),
        Arguments.of("32"),
        Arguments.of("99")
      );
    }

    private static Stream<Arguments> invalidCenturyCodes() {
      return Stream.of(
        Arguments.of("0"),
        Arguments.of("7"),
        Arguments.of("8"),
        Arguments.of("9")
      );
    }

    @Test
    @DisplayName("is blank")
    void isBlank() {
      String blankSsnValue = "\n \t";

      ThrowingCallable test = () -> new SocialSecurityNumber(blankSsnValue);

      assertThatIllegalArgumentException().isThrownBy(test);
    }

    @Test
    @DisplayName("is too long")
    void isTooLong() {
      String tooLongSsnValue = "372052502690";

      ThrowingCallable test = () -> new SocialSecurityNumber(tooLongSsnValue);

      assertThatIllegalArgumentException().isThrownBy(test);
    }

    @Test
    @DisplayName("is too short")
    void isTooShort() {
      String tooShortSsnValue = "3720525026";

      ThrowingCallable test = () -> new SocialSecurityNumber(tooShortSsnValue);

      assertThatIllegalArgumentException().isThrownBy(test);
    }

    @Test
    @DisplayName("is not numeric")
    void isNotNumeric() {
      String nonNumericSsnValue = "3720525026A";

      ThrowingCallable test = () -> new SocialSecurityNumber(nonNumericSsnValue);

      assertThatIllegalArgumentException().isThrownBy(test);
    }

    @Test
    @DisplayName("contains 31 as day of month, for a 30-day month")
    void containsThirtyOneAsDayOfMonth_for_aThirtyDayMonth() {
      String thirtyFirstDayOfMonth = "31";
      String monthWithThirtyDays = "04";
      String invalidSsnValue = format("372%s%s0269", monthWithThirtyDays, thirtyFirstDayOfMonth);

      ThrowingCallable test = () -> new SocialSecurityNumber(invalidSsnValue);

      assertThatIllegalArgumentException().isThrownBy(test);
    }

    @Test
    @DisplayName("contains birth date, which is in the future")
    void containsBirthDate_which_isInTheFuture() {
      String futureBirthDate = "950122";
      String invalidSsnValue = format("5%s4217", futureBirthDate);

      ThrowingCallable test = () -> new SocialSecurityNumber(invalidSsnValue);

      assertThatIllegalArgumentException().isThrownBy(test);
    }

    @Test
    @DisplayName("ends with invalid checksum")
    void ends_with_invalidChecksum() {
      String invalidChecksum = "3706";
      String invalidSsnValue = format("6150429%s", invalidChecksum);

      ThrowingCallable test = () -> new SocialSecurityNumber(invalidSsnValue);

      assertThatIllegalArgumentException().isThrownBy(test);
    }

    @ParameterizedTest
    @DisplayName("starts with invalid century code of")
    @MethodSource("invalidCenturyCodes")
    void starts_with_invalidCenturyCodeOf(String centuryCode) {
      String invalidSsnValue = format("%s1504293706", centuryCode);

      ThrowingCallable test = () -> new SocialSecurityNumber(invalidSsnValue);

      assertThatIllegalStateException()
        .isThrownBy(test)
        .withMessage(
          format("Illegal century value (%s) in social security number (%s)", centuryCode, invalidSsnValue)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidDaysOfMonth")
    @DisplayName("contains invalid day of month, as")
    void containsInvalidDayOfMonth(String invalidDayOfMonth) {
      String invalidSsnValue = format("37205%s0269", invalidDayOfMonth);

      Executable test = () -> new SocialSecurityNumber(invalidSsnValue);

      assertThrows(DateTimeParseException.class, test);
    }

    @Test
    @DisplayName("contains invalid month (13)")
    void contains_invalidMonth_13() {
      String invalidMonth = "13";
      String invalidSsnValue = format("615%s293707", invalidMonth);

      Executable test = () -> new SocialSecurityNumber(invalidSsnValue);

      assertThrows(DateTimeParseException.class, test);
    }
  }
}
