package ee.rsx.kata.bank.loans.domain.ssn

import java.lang.IllegalStateException
import java.time.LocalDate
import java.time.LocalDate.now
import java.time.format.DateTimeFormatter.ofPattern
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Supplier
import java.util.regex.Pattern.compile
import java.util.stream.IntStream

data class SocialSecurityNumber(val value: String) {

  /**
   * @throws IllegalArgumentException when provided SSN value is invalid (does not correspond to Estonian SSN rules)
   * @throws IllegalStateException when century prefix of provided SSN value is invalid (not between 1...6)
   * @throws DateTimeParseException when birth date part in of provided SSN does not represent a valid date
   */
  init {
    validate()
  }

  companion object {
    private val SSN_PATTERN = compile("^[0-9]\\d{2}[0-1]\\d{7}$")
    private val SSN_DATE_FORMAT = ofPattern("yyyyMMdd")
    private val DEFAULT_CHECKSUM_MULTIPLIERS = intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 1)
    private val RECALCULATED_CHECKSUM_MULTIPLIERS = intArrayOf(3, 4, 5, 6, 7, 8, 9, 1, 2, 3)
  }

  private fun validate() = require(
    SSN_PATTERN.matcher(value).matches() &&
      isValidBirthDate() &&
      calculateChecksum() == parseChecksumAtTheEnd()
  )

  private fun isValidBirthDate() = parseBirthDate().let { it.isEqual(now()) || it.isBefore(now()) }

  private fun parseBirthDate(): LocalDate {
    var date = value.substring(1, 7)
    val century = value.substring(0, 1)
    date = when (century) {
      "1", "2" -> "18$date"
      "3", "4" -> "19$date"
      "5", "6" -> "20$date"
      else -> error("Illegal century value ($century) in social security number ($value)")
    }
    return LocalDate.parse(date, SSN_DATE_FORMAT)
  }

  private fun calculateChecksum(): Int {
    val recalculation = { calculateChecksumUsing(RECALCULATED_CHECKSUM_MULTIPLIERS) { 0 } }
    return calculateChecksumUsing(DEFAULT_CHECKSUM_MULTIPLIERS, recalculation)
  }

  private fun calculateChecksumUsing(multipliers: IntArray, recalculatedChecksum: Supplier<Int>): Int {
    val total = totalOfEachSsnNumberMultipliedWith(multipliers)
    var modulus = total % 11
    if (isDoubleDigit(modulus)) {
      modulus = recalculatedChecksum.get()
    }
    return modulus
  }

  private fun totalOfEachSsnNumberMultipliedWith(multipliers: IntArray): Int {
    val total = AtomicInteger()
    IntStream
      .range(0, value.length - 1)
      .forEach { index: Int -> total.addAndGet(numberAt(index) * multipliers[index]) }
    return total.get()
  }

  private fun numberAt(index: Int) = value.substring(index, index + 1).toInt()

  private fun isDoubleDigit(modulus: Int) = 10 == modulus

  private fun parseChecksumAtTheEnd() = value.substring(value.length - 1).toInt()
}
