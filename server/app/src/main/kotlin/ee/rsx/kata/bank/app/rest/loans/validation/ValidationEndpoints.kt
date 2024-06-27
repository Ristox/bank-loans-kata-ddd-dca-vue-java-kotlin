package ee.rsx.kata.bank.app.rest.loans.validation

import ee.rsx.kata.bank.loans.validation.limits.LoadValidationLimits
import ee.rsx.kata.bank.loans.validation.ssn.ValidateSocialSecurityNumber
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/loans/validation")
class ValidationEndpoints(
  private val loadLimits: LoadValidationLimits,
  private val validateSocialSecurityNumber: ValidateSocialSecurityNumber
) {

  @GetMapping(value = ["/limits"])
  fun loadValidationLimits() = loadLimits()

  @GetMapping(value = ["/ssn"])
  fun validateSsn(@RequestParam("value") value: String) = validateSocialSecurityNumber(value)
}
