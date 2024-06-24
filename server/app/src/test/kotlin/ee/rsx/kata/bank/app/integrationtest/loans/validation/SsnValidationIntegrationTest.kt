package ee.rsx.kata.bank.app.integrationtest.loans.validation

import ee.rsx.kata.bank.app.Server
import jakarta.inject.Inject
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [Server::class])
@AutoConfigureMockMvc
@DisplayName("SSN Validation for loans eligibility request")
class SsnValidationIntegrationTest {

  companion object {
    private const val SSN_VALIDATION_URL = "/loans/validation/ssn?value="
  }

  @Inject
  private lateinit var mockMvc: MockMvc

  @Test
  fun `returns OK result for valid provided SSN`() {
    val validProvidedSsn = "49002010965"
    val expectedOkResult = "OK"

    mockMvc.perform(get("$SSN_VALIDATION_URL$validProvidedSsn"))
      .andExpect(status().isOk())
      .andExpect(
        content().json(
          """
            {
              "ssn": "$validProvidedSsn",
              "status": "$expectedOkResult"
            }
          """.trimIndent()
        )
      )
  }

  @Test
  fun `returns INVALID result for invalid provided SSN`() {
    val invalidProvidedSsn = "89002010965"
    val expectedInvalidResult = "INVALID"

    mockMvc.perform(get("$SSN_VALIDATION_URL$invalidProvidedSsn"))
      .andExpect(status().isOk())
      .andExpect(
        content().json(
          """
            {
              "ssn": "$invalidProvidedSsn",
              "status": "$expectedInvalidResult"
            }
          """.trimIndent()
        )
      )
  }
}
