package ee.rsx.kata.bank.app.integrationtest.loans.validation;

import ee.rsx.kata.bank.app.Server;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = {Server.class})
@AutoConfigureMockMvc
@DisplayName("Validation limits for loans eligibility request")
public class ValidationLimitsIntegrationTest {

  private static final String VALIDATION_LIMITS_URL = "/loans/validation/limits";

  @Inject
  private MockMvc mockMvc;

  @Test
  @DisplayName("returns expected validation limits (min and max loan as well as period")
  void returns_expectedValidationLimits_minAndMaxLoanAsWellAsPeriod() throws Exception {
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
        )
      );
  }
}
