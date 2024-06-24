package ee.rsx.kata.bank.app.integrationtest.loans.eligibility;

import ee.rsx.kata.bank.app.Server;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

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
  @DisplayName("returns an DENIED eligibility result for the provided loan request, since person is in DEBT")
  void returns_DENIED_result_forProvidedLoanRequest_sincePersonInDebt() throws Exception {
    var personInDebt = "49002010965";
    var loanEligibilityRequest = """
        {
          "ssn": "%s",
          "loanAmount": 4500,
          "loanPeriodMonths": 36
        }
      """.formatted(personInDebt);
    var postRequest =
      post(LOAN_ELIGIBILITY_URL)
        .contentType(APPLICATION_JSON)
        .content(loanEligibilityRequest);

    var notAcceptableStatus = status().isNotAcceptable();
    mockMvc.perform(postRequest)
      .andExpect(notAcceptableStatus)
      .andExpect(
        content().json(
          """
              {
                "result": "DENIED",
                "ssn": "%s",
                "loanAmount": 4500,
                "loanPeriodMonths": 36
              }
            """.formatted(personInDebt)
        )
      );
  }

  @Test
  @DisplayName("returns an APPROVED eligibility result for the provided loan request, since person is in high SEGMENT 3")
  void returns_APPROVED_result_forProvidedLoanRequest_sincePersonInHighCreditSegment() throws Exception {
    var highCreditPerson = "49002010998";
    var loanEligibilityRequest = """
        {
          "ssn": "%s",
          "loanAmount": 4500,
          "loanPeriodMonths": 36
        }
      """.formatted(highCreditPerson);
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
                "ssn": "%s",
                "loanAmount": 4500,
                "loanPeriodMonths": 36,
                "eligibleLoanAmount": 10000
              }
            """.formatted(highCreditPerson)
        )
      );
  }

  @Test
  @DisplayName("returns an APPROVED eligibility result for the provided loan request, since person is in good SEGMENT 2")
  void returns_APPROVED_result_forProvidedLoanRequest_sincePersonInGoodCreditSegment() throws Exception {
    var goodCreditPerson = "49002010987";
    var loanEligibilityRequest = """
        {
          "ssn": "%s",
          "loanAmount": 4500,
          "loanPeriodMonths": 36
        }
      """.formatted(goodCreditPerson);
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
                "ssn": "%s",
                "loanAmount": 4500,
                "loanPeriodMonths": 36
              }
            """.formatted(goodCreditPerson)
        )
      );
  }

  @Test
  @DisplayName("returns a DENIED eligibility result for the provided loan request, since person is in low SEGMENT 1")
  void returns_DENIED_result_forProvidedLoanRequest_sincePersonInLowCreditSegment() throws Exception {
    var lowCreditPerson = "49002010976";
    var loanEligibilityRequest = """
        {
          "ssn": "%s",
          "loanAmount": 4500,
          "loanPeriodMonths": 36
        }
      """.formatted(lowCreditPerson);
    var postRequest =
      post(LOAN_ELIGIBILITY_URL)
        .contentType(APPLICATION_JSON)
        .content(loanEligibilityRequest);

    var notAcceptableStatus = status().isNotAcceptable();
    mockMvc.perform(postRequest)
      .andExpect(notAcceptableStatus)
      .andDo(MockMvcResultHandlers.print())
      .andExpect(
        content().json(
          """
              {
                "result": "DENIED",
                "ssn": "%s",
                "loanAmount": 4500,
                "loanPeriodMonths": 36,
                "eligibleLoanAmount": 3599
              }
            """.formatted(lowCreditPerson)
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
