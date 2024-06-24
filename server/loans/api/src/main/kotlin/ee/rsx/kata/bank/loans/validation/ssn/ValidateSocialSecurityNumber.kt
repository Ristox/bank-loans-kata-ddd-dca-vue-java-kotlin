package ee.rsx.kata.bank.loans.validation.ssn

fun interface ValidateSocialSecurityNumber {
  fun on(ssn: String): SsnValidationResultDTO
}
