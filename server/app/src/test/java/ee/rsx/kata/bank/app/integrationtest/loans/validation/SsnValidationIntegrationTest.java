package ee.rsx.kata.bank.app.integrationtest.loans.validation;

import ee.rsx.kata.bank.app.Server;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static java.lang.String.format;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = {Server.class})
@AutoConfigureMockMvc
@DisplayName("SSN Validation for loans eligibility request")
public class SsnValidationIntegrationTest {

  private static final String SSN_VALIDATION_URL = "/loans/validation/ssn?value=%s";

  @Inject
  private MockMvc mockMvc;

  @Test
  @DisplayName("returns OK result for valid provided SSN")
  void returns_OK_result_forValidProvidedSsn() throws Exception {
    var validProvidedSsn = "49002010965";
    var expectedOkResult = "OK";

    mockMvc.perform(get(
        format(SSN_VALIDATION_URL, validProvidedSsn)
      ))
      .andExpect(status().isOk())
      .andExpect(
        content().json(
          """
              {
                "ssn": "%s",
                "status": "%s"
              }
            """.formatted(validProvidedSsn, expectedOkResult)
        )
      );
  }

  @Test
  @DisplayName("returns INVALID result for invalid provided SSN")
  void returns_INVALID_result_forInvalidProvidedSsn() throws Exception {
    var invalidProvidedSsn = "89002010965";
    var expectedInvalidResult = "INVALID";

    mockMvc.perform(get(
        format(SSN_VALIDATION_URL, invalidProvidedSsn)
      ))
      .andExpect(status().isOk())
      .andExpect(
        content().json(
          """
              {
                "ssn": "%s",
                "status": "%s"
              }
            """.formatted(invalidProvidedSsn, expectedInvalidResult)
        )
      );
  }
}
