package ee.rsx.kata.bank.loans.usecases;

import ee.rsx.kata.bank.loans.validation.ssn.SsnValidationResultDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static ee.rsx.kata.bank.loans.validation.ssn.ValidationStatus.*;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SSN Validation")
class SsnValidationTest {

  private SsnValidation ssnValidation;

  @BeforeEach
  void setup() {
    ssnValidation = new SsnValidation();
  }

  @Test
  @DisplayName("result is OK, for a given valid SSN value")
  void resultIs_OK_for_givenValidSsnValue() {
    String validSsnValue = "50212104262";

    SsnValidationResultDTO result = ssnValidation.on(validSsnValue);

    assertThat(result).isEqualTo(
      new SsnValidationResultDTO(validSsnValue, OK)
    );
  }

  @Nested
  @DisplayName("result is INVALID, for a given SSN value")
  class ResultIsInvalidForAGivenSsnValue {

    private static SsnValidationResultDTO expectedInvalidResultFor(String invalidSsnValue) {
      return new SsnValidationResultDTO(invalidSsnValue, INVALID);
    }

    @Test
    @DisplayName("having invalid date")
    void having_invalidDate() {
      String invalidDate = "021310";
      String invalidSsnValue = format("5%s4262", invalidDate);

      SsnValidationResultDTO result = ssnValidation.on(invalidSsnValue);

      assertThat(result).isEqualTo(
        expectedInvalidResultFor(invalidSsnValue)
      );
    }

    @Test
    @DisplayName("having invalid checksum")
    void having_invalidChecksum() {
      String invalidChecksum = "3";
      String invalidSsnValue = format("5021210426%s", invalidChecksum);

      SsnValidationResultDTO result = ssnValidation.on(invalidSsnValue);

      assertThat(result).isEqualTo(
        expectedInvalidResultFor(invalidSsnValue)
      );
    }

    @Test
    @DisplayName("having invalid century prefix")
    void having_invalidCenturyPrefix() {
      String invalidCenturyPrefix = "7";
      String invalidSsnValue = format("%s0212104262", invalidCenturyPrefix);

      SsnValidationResultDTO result = ssnValidation.on(invalidSsnValue);

      assertThat(result).isEqualTo(
        expectedInvalidResultFor(invalidSsnValue)
      );
    }
  }
}
