package ee.rsx.kata.bank.app.integrationtest.loans.eligibility;

import ee.rsx.kata.bank.app.Server;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
  void returns_approved_result_forProvidedLoanRequest() throws Exception {
    String loanEligibilityRequest = """
        {
          "ssn": "49002010965",
          "loanAmount": 4500,
          "loanPeriodMonths": 36
        }
      """;
    MockHttpServletRequestBuilder postRequest =
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
}
