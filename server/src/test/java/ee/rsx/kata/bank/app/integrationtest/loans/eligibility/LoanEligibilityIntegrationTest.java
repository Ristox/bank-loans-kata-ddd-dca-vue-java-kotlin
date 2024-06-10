package ee.rsx.kata.bank.app.integrationtest.loans.eligibility;

import ee.rsx.kata.bank.app.Server;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = {Server.class})
@AutoConfigureMockMvc
@DisplayName("Loan eligibility calculation")
public class LoanEligibilityIntegrationTest {

  private static final String LOAN_ELIGIBILITY_URL = "/loans/eligibility";

  @Inject
  private MockMvc mockMvc;

  @Test
  @DisplayName("returns an APPROVED eligibility result for the provided loan request")
  void returns_APPROVED_result_forProvidedLoanRequest() throws Exception {
    var loanEligibilityRequest = """
        {
          "ssn": "49002010965",
          "loanAmount": 4500,
          "loanPeriodMonths": 36
        }
      """;
    var postRequest =
      post(LOAN_ELIGIBILITY_URL)
        .contentType(APPLICATION_JSON)
        .content(loanEligibilityRequest);

    mockMvc.perform(postRequest)
      .andExpect(status().isOk())
      .andExpect(
        content().json(
          """
              {
                "result": "APPROVED",
                "ssn": "49002010965",
                "loanAmount": 4500,
                "loanPeriodMonths": 36
              }
            """
        )
      );
  }

  @Test
  @DisplayName("returns an INVALID result with validation error details, for an invalid loan request")
  void returns_INVALID_result_with_validationErrorDetails_forInvalidLoanRequest() throws Exception {
    var ssnWithInvalidChecksum = "49002010968";
    var tooSmallLoanAmount = 1500;
    var tooLargeLoanPeriod = 61;
    var loanEligibilityRequest = """
        {
          "ssn": "%s",
          "loanAmount": %s,
          "loanPeriodMonths": %s
        }
      """.formatted(ssnWithInvalidChecksum, tooSmallLoanAmount, tooLargeLoanPeriod);

    var postRequest =
      post(LOAN_ELIGIBILITY_URL)
        .contentType(APPLICATION_JSON)
        .content(loanEligibilityRequest);

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
                "ssn": "%s",
                "loanAmount": %s,
                "loanPeriodMonths": %s
              }
            """.formatted(ssnWithInvalidChecksum, tooSmallLoanAmount, tooLargeLoanPeriod)
        )
      );
  }
}