package ee.rsx.kata.bank.loans.validation.ssn

@JvmRecord
data class SsnValidationResultDTO(val ssn: String, val status: ValidationStatus) {
  companion object {
    fun okResultWith(ssn: String): SsnValidationResultDTO {
      return SsnValidationResultDTO(ssn, ValidationStatus.OK)
    }

    fun invalidResultWith(ssn: String): SsnValidationResultDTO {
      return SsnValidationResultDTO(ssn, ValidationStatus.INVALID)
    }
  }
}
