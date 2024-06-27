package ee.rsx.kata.bank.app.rest.loans.eligibility

import ee.rsx.kata.bank.loans.eligibility.CalculateLoanEligibility
import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityRequestDTO
import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityResultDTO
import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityStatus.APPROVED
import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityStatus.DENIED
import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityStatus.INVALID
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_ACCEPTABLE
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/loans/eligibility")
class LoanEligibilityEndpoint(
  private val calculateEligibility: CalculateLoanEligibility
) {

  @PostMapping
  fun calculateLoanEligibility(
    @RequestBody request: LoanEligibilityRequestDTO
  ): ResponseEntity<LoanEligibilityResultDTO> =
    with(calculateEligibility(request)) {
      val httpStatus = when (result) {
        APPROVED -> OK
        INVALID -> BAD_REQUEST
        DENIED -> NOT_ACCEPTABLE
      }

      ResponseEntity.status(httpStatus)
        .contentType(APPLICATION_JSON)
        .body(this)
    }
}
