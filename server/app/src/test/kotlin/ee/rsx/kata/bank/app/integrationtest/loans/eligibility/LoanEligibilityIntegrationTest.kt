package ee.rsx.kata.bank.app.integrationtest.loans.eligibility

import ee.rsx.kata.bank.app.Server
import jakarta.inject.Inject
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = [Server::class])
@AutoConfigureMockMvc
@DisplayName("Loan eligibility calculation")
class LoanEligibilityIntegrationTest {

  companion object {
    private const val LOAN_ELIGIBILITY_URL = "/loans/eligibility"
  }

  @Inject
  private lateinit var mockMvc: MockMvc

  @Test
  fun `returns an DENIED eligibility result for the provided loan request, since person is in DEBT`() {
    val personInDebt = "49002010965"
    val loanEligibilityRequest = """
        {
          "ssn": "$personInDebt",
          "loanAmount": 4500,
          "loanPeriodMonths": 36
        }
      
      """.trimIndent()

    val postRequest = post(LOAN_ELIGIBILITY_URL)
      .contentType(APPLICATION_JSON)
      .content(loanEligibilityRequest)

    val notAcceptableStatus = status().isNotAcceptable()
    mockMvc.perform(postRequest)
      .andExpect(notAcceptableStatus)
      .andExpect(
        content().json(
          """
            {
              "result": "DENIED",
              "ssn": "$personInDebt",
              "loanAmount": 4500,
              "loanPeriodMonths": 36
            }
            """.trimIndent()
        )
      )
  }

  @Test
  fun `returns an APPROVED eligibility result for the provided loan request, since person is in high SEGMENT 3`() {
    val highCreditPerson = "49002010998"
    val loanEligibilityRequest = """
        {
          "ssn": "$highCreditPerson",
          "loanAmount": 4500,
          "loanPeriodMonths": 36
        }
        """.trimIndent()

    val postRequest = post(LOAN_ELIGIBILITY_URL)
      .contentType(APPLICATION_JSON)
      .content(loanEligibilityRequest)

    mockMvc.perform(postRequest)
      .andExpect(status().isOk())
      .andExpect(
        content().json(
          """
            {
              "result": "APPROVED",
              "ssn": "$highCreditPerson",
              "loanAmount": 4500,
              "loanPeriodMonths": 36,
              "eligibleLoanAmount": 10000
            }
          """.trimIndent()
        )
      )
  }

  @Test
  fun `returns an APPROVED eligibility result for the provided loan request, since person is in good SEGMENT 2`() {
    val goodCreditPerson = "49002010987"
    val loanEligibilityRequest = """
        {
          "ssn": "$goodCreditPerson",
          "loanAmount": 4500,
          "loanPeriodMonths": 36
        }
      
      """.trimIndent()

    val postRequest = post(LOAN_ELIGIBILITY_URL)
      .contentType(APPLICATION_JSON)
      .content(loanEligibilityRequest)

    mockMvc.perform(postRequest)
      .andExpect(status().isOk())
      .andExpect(
        content().json(
          """
            {
              "result": "APPROVED",
              "ssn": "$goodCreditPerson",
              "loanAmount": 4500,
              "loanPeriodMonths": 36
            }
          """.trimIndent()
        )
      )
  }

  @Test
  fun `returns a DENIED eligibility result for the provided loan request, since person is in low SEGMENT 1`() {
    val lowCreditPerson = "49002010976"
    val loanEligibilityRequest = """
        {
          "ssn": "$lowCreditPerson",
          "loanAmount": 4500,
          "loanPeriodMonths": 36
        }
      
      """.trimIndent()

    val postRequest = post(LOAN_ELIGIBILITY_URL)
      .contentType(APPLICATION_JSON)
      .content(loanEligibilityRequest)

    val notAcceptableStatus = status().isNotAcceptable()
    mockMvc.perform(postRequest)
      .andExpect(notAcceptableStatus)
      .andExpect(
        content().json(
          """
            {
              "result": "DENIED",
              "ssn": "$lowCreditPerson",
              "loanAmount": 4500,
              "loanPeriodMonths": 36,
              "eligibleLoanAmount": 3599
            }
          """.trimIndent()
        )
      )
  }

  @Test
  fun `returns an INVALID result with validation error details, for an invalid loan request`() {
    val ssnWithInvalidChecksum = "49002010968"
    val tooSmallLoanAmount = 1500
    val tooLargeLoanPeriod = 61
    val loanEligibilityRequest = """
        {
          "ssn": "$ssnWithInvalidChecksum",
          "loanAmount": $tooSmallLoanAmount,
          "loanPeriodMonths": $tooLargeLoanPeriod
        }
      
      """.trimIndent()

    val postRequest = post(LOAN_ELIGIBILITY_URL)
      .contentType(APPLICATION_JSON)
      .content(loanEligibilityRequest)

    mockMvc.perform(postRequest)
      .andExpect(status().isBadRequest())
      .andExpect(
        content().json(
          """
            {
              "result": "INVALID",
              "errors": [
                "SSN is not valid",
                "Loan amount is less than minimum required",
                "Loan period is more than maximum allowed"
              ],
              "ssn": "$ssnWithInvalidChecksum",
              "loanAmount": $tooSmallLoanAmount,
              "loanPeriodMonths": $tooLargeLoanPeriod
            }
          """.trimIndent()
        )
      )
  }
}
