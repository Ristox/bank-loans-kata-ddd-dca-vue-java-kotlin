package ee.rsx.kata.bank.loans.validation.ssn

fun interface ValidateSocialSecurityNumber {
  operator fun invoke(ssn: String): SsnValidationResultDTO
}
