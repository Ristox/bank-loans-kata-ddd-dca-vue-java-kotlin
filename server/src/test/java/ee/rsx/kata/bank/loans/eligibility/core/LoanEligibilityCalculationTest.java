package ee.rsx.kata.bank.loans.eligibility.core;

import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityRequestDTO;
import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityResultDTO;
import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityStatus;
import ee.rsx.kata.bank.loans.validation.LoadValidationLimits;
import ee.rsx.kata.bank.loans.validation.SsnValidationResultDTO;
import ee.rsx.kata.bank.loans.validation.ValidateSocialSecurityNumber;
import ee.rsx.kata.bank.loans.validation.ValidationLimitsDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.util.List;

import static ee.rsx.kata.bank.loans.eligibility.LoanEligibilityStatus.*;
import static ee.rsx.kata.bank.loans.validation.SsnValidationResultDTO.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@DisplayName("Loan eligibility calculation")
@ExtendWith(MockitoExtension.class)
class LoanEligibilityCalculationTest {

  private static final Integer MINIMUM_REQUIRED_LOAN_AMOUNT = 2_000;
  private static final Integer MAXIMUM_ALLOWED_LOAN_AMOUNT = 10_000;

  private static final Integer MINIMUM_REQUIRED_LOAN_PERIOD = 12;
  private static final Integer MAXIMUM_ALLOWED_LOAN_PERIOD = 60;

  private static final ValidationLimitsDTO TEST_VALIDATION_LIMITS =
    new ValidationLimitsDTO(
      MINIMUM_REQUIRED_LOAN_AMOUNT,
      MAXIMUM_ALLOWED_LOAN_AMOUNT,
      MINIMUM_REQUIRED_LOAN_PERIOD,
      MAXIMUM_ALLOWED_LOAN_PERIOD
    );

  private static Answer<SsnValidationResultDTO> okResultWithProvidedSsn() {
    return methodCall -> {
      String providedSsn = methodCall.getArgument(0);
      return okResultWith(providedSsn);
    };
  }

  @Mock
  private ValidateSocialSecurityNumber validateSocialSecurityNumber;

  @Mock
  private LoadValidationLimits loadValidationLimits;

  @InjectMocks
  private LoanEligibilityCalculation calculateLoanEligibility;

  @BeforeEach
  void setup() {
    when(validateSocialSecurityNumber.on(anyString()))
      .thenAnswer(okResultWithProvidedSsn());

    when(loadValidationLimits.invoke())
      .thenReturn(TEST_VALIDATION_LIMITS);
  }

  @Test
  @DisplayName("returns APPROVED result along with provided valid eligibility request data")
  void returns_APPROVED_result_with_providedValidEligibilityRequestData() {
    LoanEligibilityRequestDTO validRequest = new LoanEligibilityRequestDTO("49002010965", 4500, 36);

    LoanEligibilityResultDTO result = calculateLoanEligibility.on(validRequest);

    assertThat(result)
      .isEqualTo(new LoanEligibilityResultDTO(APPROVED, null, "49002010965", 4500, 36));
  }

  @Test
  @DisplayName("returns INVALID result with SSN error message, when invalid SSN provided")
  void returns_INVALID_result_with_ssnErrorMessage_whenInvalidSsnProvided() {
    String invalidSsn = "49002010966";
    whenSsnValidationFailsFor(invalidSsn);
    LoanEligibilityRequestDTO invalidRequest = new LoanEligibilityRequestDTO(invalidSsn, 4500, 36);

    LoanEligibilityResultDTO result = calculateLoanEligibility.on(invalidRequest);

    assertThat(result)
      .isEqualTo(new LoanEligibilityResultDTO(INVALID, List.of("SSN is not valid"), invalidSsn, 4500, 36));
  }

  private void whenSsnValidationFailsFor(String invalidSsn) {
    when(validateSocialSecurityNumber.on(invalidSsn)).thenReturn(invalidResultWith(invalidSsn));
  }

  @Test
  @DisplayName("returns INVALID result with error message on too small loan amount, when too small loan amount provided")
  void returns_INVALID_result_with_loanAmountTooSmallMessage_whenTooSmallLoanAmountProvided() {
    Integer tooSmallLoanAmount = MINIMUM_REQUIRED_LOAN_AMOUNT - 1;
    LoanEligibilityRequestDTO invalidRequest = new LoanEligibilityRequestDTO("49002010965", tooSmallLoanAmount, 36);

    LoanEligibilityResultDTO result = calculateLoanEligibility.on(invalidRequest);

    assertThat(result)
      .isEqualTo(
        new LoanEligibilityResultDTO(INVALID, List.of("Loan amount is less than minimum required"), "49002010965", tooSmallLoanAmount, 36)
      );
  }

  @Test
  @DisplayName("returns INVALID result with error message on too large loan amount, when too large loan amount provided")
  void returns_INVALID_result_with_loanAmountTooLargeMessage_whenTooLargeLoanAmountProvided() {
    Integer tooLargeLoanAmount = MAXIMUM_ALLOWED_LOAN_AMOUNT + 1;
    LoanEligibilityRequestDTO invalidRequest = new LoanEligibilityRequestDTO("49002010965", tooLargeLoanAmount, 36);

    LoanEligibilityResultDTO result = calculateLoanEligibility.on(invalidRequest);

    assertThat(result)
      .isEqualTo(
        new LoanEligibilityResultDTO(INVALID, List.of("Loan amount is more than maximum allowed"), "49002010965", tooLargeLoanAmount, 36)
      );
  }

  @Test
  @DisplayName("returns INVALID result with error message on too small loan period, when too small loan period provided")
  void returns_INVALID_result_with_loanPeriodTooSmallMessage_whenTooSmallLoanPeriodProvided() {
    Integer tooSmallLoanPeriod = MINIMUM_REQUIRED_LOAN_PERIOD - 1;
    LoanEligibilityRequestDTO invalidRequest = new LoanEligibilityRequestDTO("49002010965", 4500, tooSmallLoanPeriod);

    LoanEligibilityResultDTO result = calculateLoanEligibility.on(invalidRequest);

    assertThat(result)
      .isEqualTo(
        new LoanEligibilityResultDTO(INVALID, List.of("Loan period is less than minimum required"), "49002010965", 4500, tooSmallLoanPeriod)
      );
  }

  @Test
  @DisplayName("returns INVALID result with error message on too large loan period, when too big loan period provided")
  void returns_INVALID_result_with_loanPeriodTooLargeMessage_whenTooLargeLoanPeriodProvided() {
    Integer tooLargeLoanPeriod = MAXIMUM_ALLOWED_LOAN_PERIOD + 1;
    LoanEligibilityRequestDTO invalidRequest = new LoanEligibilityRequestDTO("49002010965", 4500, tooLargeLoanPeriod);

    LoanEligibilityResultDTO result = calculateLoanEligibility.on(invalidRequest);

    assertThat(result)
      .isEqualTo(
        new LoanEligibilityResultDTO(INVALID, List.of("Loan period is more than maximum allowed"), "49002010965", 4500, tooLargeLoanPeriod)
      );
  }

  @Test
  @DisplayName("returns INVALID result with several error messages, when eligibility result contains several invalid details")
  void returns_INVALID_result_with_severalErrorMessages_whenEligibilityResult_contains_severalInvalidDetails() {
    String invalidSsn = "49002010966";
    whenSsnValidationFailsFor(invalidSsn);
    Integer tooSmallLoanAmount = MINIMUM_REQUIRED_LOAN_AMOUNT - 1;
    Integer tooLargeLoanPeriod = MAXIMUM_ALLOWED_LOAN_PERIOD + 1;
    LoanEligibilityRequestDTO invalidRequest = new LoanEligibilityRequestDTO(invalidSsn, tooSmallLoanAmount, tooLargeLoanPeriod);

    LoanEligibilityResultDTO result = calculateLoanEligibility.on(invalidRequest);

    assertThat(result)
      .isEqualTo(
        new LoanEligibilityResultDTO(
          INVALID,
          List.of(
            "SSN is not valid",
            "Loan amount is less than minimum required",
            "Loan period is more than maximum allowed"
          ),
          invalidSsn,
          tooSmallLoanAmount,
          tooLargeLoanPeriod)
      );
  }
}
