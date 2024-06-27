package ee.rsx.kata.bank.loans.validation.limits

fun interface LoadValidationLimits {
  operator fun invoke(): ValidationLimitsDTO
}
