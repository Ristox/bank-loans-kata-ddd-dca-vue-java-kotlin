package ee.rsx.kata.bank.app.integrationtest.loans.validation

import ee.rsx.kata.bank.app.Server
import jakarta.inject.Inject
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = [Server::class])
@AutoConfigureMockMvc
@DisplayName("Validation limits for loans eligibility request")
class ValidationLimitsIntegrationTest {

  companion object {
    private const val VALIDATION_LIMITS_URL = "/loans/validation/limits"
  }

  @Inject
  private lateinit var mockMvc: MockMvc

  @Test
  fun `returns expected validation limits (min and max loan as well as period)`() {
    mockMvc.perform(get(VALIDATION_LIMITS_URL))
      .andExpect(status().isOk())
      .andExpect(
        content().json(
          """
              {
                "minimumLoanAmount": 2000,
                "maximumLoanAmount": 10000,
                "minimumLoanPeriodMonths": 12,
                "maximumLoanPeriodMonths": 60
              }
            
            """
            .trimIndent()
        )
      )
  }
}
