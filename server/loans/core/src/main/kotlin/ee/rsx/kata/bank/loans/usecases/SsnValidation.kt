package ee.rsx.kata.bank.loans.usecases

import ee.rsx.kata.bank.loans.domain.ssn.SocialSecurityNumber
import ee.rsx.kata.bank.loans.validation.ssn.SsnValidationResultDTO
import ee.rsx.kata.bank.loans.validation.ssn.SsnValidationResultDTO.invalidResultWith
import ee.rsx.kata.bank.loans.validation.ssn.SsnValidationResultDTO.okResultWith
import ee.rsx.kata.bank.loans.validation.ssn.ValidateSocialSecurityNumber
import jakarta.inject.Named
import java.time.format.DateTimeParseException

@Named
internal class SsnValidation : ValidateSocialSecurityNumber {

  override fun on(ssn: String): SsnValidationResultDTO =
    try {
      val validSsn = SocialSecurityNumber(ssn)
      okResultWith(validSsn.value)
    } catch (e: RuntimeException) {
      when (e) {
        is IllegalArgumentException,
        is IllegalStateException,
        is DateTimeParseException -> invalidResultWith(ssn)
        else -> throw e
      }
    }
}
