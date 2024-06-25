package ee.rsx.kata.bank.loans.usecases

import ee.rsx.kata.bank.loans.validation.ssn.SsnValidationResultDTO
import ee.rsx.kata.bank.loans.validation.ssn.ValidationStatus.INVALID
import ee.rsx.kata.bank.loans.validation.ssn.ValidationStatus.OK
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("SSN Validation")
internal class SsnValidationTest {

  private lateinit var validateSsn: SsnValidation

  @BeforeEach
  fun setup() {
    validateSsn = SsnValidation()
  }

  @Test
  fun `result is OK, for a given valid SSN value`() {
    val validSsnValue = "50212104262"

    val result = validateSsn(validSsnValue)

    assertThat(result).isEqualTo(
      SsnValidationResultDTO(validSsnValue, OK)
    )
  }

  @Nested
  @DisplayName("result is INVALID, for a given SSN value")
  internal inner class ResultIsInvalidForAGivenSsnValue {

    @Test
    fun `having invalid date`() {
      val invalidDate = "021310"
      val invalidSsnValue = "5${invalidDate}4262"

      val result = validateSsn(invalidSsnValue)

      assertThat(result).isEqualTo(
        expectedInvalidResultFor(invalidSsnValue)
      )
    }

    @Test
    fun `having invalid checksum`() {
      val invalidChecksum = "3"
      val invalidSsnValue = "5021210426${invalidChecksum}"

      val result = validateSsn(invalidSsnValue)

      assertThat(result).isEqualTo(
        expectedInvalidResultFor(invalidSsnValue)
      )
    }

    @Test
    fun `having invalid century prefix`() {
      val invalidCenturyPrefix = "7"
      val invalidSsnValue = "${invalidCenturyPrefix}0212104262"

      val result = validateSsn(invalidSsnValue)

      assertThat(result).isEqualTo(
        expectedInvalidResultFor(invalidSsnValue)
      )
    }
  }

  companion object {
    private fun expectedInvalidResultFor(invalidSsnValue: String) =
      SsnValidationResultDTO(invalidSsnValue, INVALID)
  }
}
