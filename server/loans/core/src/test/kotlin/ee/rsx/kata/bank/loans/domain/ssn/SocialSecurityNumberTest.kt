package ee.rsx.kata.bank.loans.domain.ssn

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.format.DateTimeParseException
import java.util.stream.Stream

@DisplayName("Social Security Number")
internal class SocialSecurityNumberTest {

  @ParameterizedTest
  @MethodSource("validSocialSecurityNumbers")
  fun `Is created, when constructed with a valid ssn value of`(ssnValue: String) {
    assertDoesNotThrow {
      SocialSecurityNumber(ssnValue)
    }
  }

  @Test
  fun `has value equal to the valid provided ssn value`() {
    val providedSsnValue = "49002010998"

    val validSsn = SocialSecurityNumber(providedSsnValue)

    assertThat(validSsn.value).isEqualTo(providedSsnValue)
  }

  @Nested
  @TestInstance(PER_CLASS)
  @DisplayName("Creation fails, when SSN value")
  inner class CreationFailsWhenSsnValue {

    @Test
    fun `is blank`() {
      val blankSsnValue = "\n \t"

      val test: () -> Unit = { SocialSecurityNumber(blankSsnValue) }

      assertThatIllegalArgumentException().isThrownBy(test)
    }

    @Test
    fun `is too long`() {
      val tooLongSsnValue = "372052502690"

      val test: () -> Unit = { SocialSecurityNumber(tooLongSsnValue) }

      assertThatIllegalArgumentException().isThrownBy(test)
    }

    @Test
    fun `is too short`() {
      val tooShortSsnValue = "3720525026"

      val test: () -> Unit = { SocialSecurityNumber(tooShortSsnValue) }

      assertThatIllegalArgumentException().isThrownBy(test)
    }

    @Test
    fun `is not numeric`() {
      val nonNumericSsnValue = "3720525026A"

      val test: () -> Unit = { SocialSecurityNumber(nonNumericSsnValue) }

      assertThatIllegalArgumentException().isThrownBy(test)
    }

    @Test
    fun `contains 31 as day of month, for a 30-day month`() {
      val thirtyFirstDayOfMonth = "31"
      val monthWithThirtyDays = "04"
      val invalidSsnValue = "372$monthWithThirtyDays${thirtyFirstDayOfMonth}0269"

      val test: () -> Unit = { SocialSecurityNumber(invalidSsnValue) }

      assertThatIllegalArgumentException().isThrownBy(test)
    }

    @Test
    fun `contains birth date, which is in the future`() {
      val futureBirthDate = "950122"
      val invalidSsnValue = "5${futureBirthDate}4217"

      val test: () -> Unit = { SocialSecurityNumber(invalidSsnValue) }

      assertThatIllegalArgumentException().isThrownBy(test)
    }

    @Test
    fun `ends with invalid checksum`() {
      val invalidChecksum = "6"
      val invalidSsnValue = "6150429370$invalidChecksum"

      val test: () -> Unit = { SocialSecurityNumber(invalidSsnValue) }

      assertThatIllegalArgumentException().isThrownBy(test)
    }

    @ParameterizedTest
    @MethodSource("invalidCenturyCodes")
    fun `starts with invalid century code of`(centuryCode: String) {
      val invalidSsnValue = "${centuryCode}1504293706"

      val test: () -> Unit = { SocialSecurityNumber(invalidSsnValue) }

      assertThrows<IllegalStateException>(test)
        .apply {
          assertThat(message).isEqualTo("Illegal century value ($centuryCode) in social security number ($invalidSsnValue)")
        }
    }

    @ParameterizedTest
    @MethodSource("invalidDaysOfMonth")
    fun `contains invalid day of month, as`(invalidDayOfMonth: String) {
      val invalidSsnValue = "37205${invalidDayOfMonth}0269"

      val test: () -> Unit = { SocialSecurityNumber(invalidSsnValue) }

      assertThrows(DateTimeParseException::class.java, test)
    }

    @Test
    fun `contains invalid month (13)`() {
      val invalidMonth = "13"
      val invalidSsnValue = "615${invalidMonth}293707"

      val test: () -> Unit = { SocialSecurityNumber(invalidSsnValue) }

      assertThrows(DateTimeParseException::class.java, test)
    }


    private fun invalidCenturyCodes() = Stream.of(
      Arguments.of("0"),
      Arguments.of("7"),
      Arguments.of("8"),
      Arguments.of("9")
    )

    private fun invalidDaysOfMonth() = Stream.of(
      Arguments.of("00"),
      Arguments.of("32"),
      Arguments.of("99")
    )
  }

  companion object {

    @JvmStatic
    private fun validSocialSecurityNumbers() = Stream.of(
      ssnArg("46301055224"),
      ssnArg("37205250269"),
      ssnArg("50212104262"),
      ssnArg("34110140248"),
      ssnArg("49002010976"),
      ssnArg("49002010965"),
      ssnArg("49002010998"),
      ssnArg("49002010987"),
      ssnArg("61504293707")
    )


    private fun ssnArg(ssn: String) = Arguments.of(ssn)
  }
}
