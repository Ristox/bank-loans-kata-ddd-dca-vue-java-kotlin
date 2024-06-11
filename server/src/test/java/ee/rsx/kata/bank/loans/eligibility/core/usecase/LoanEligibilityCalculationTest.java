package ee.rsx.kata.bank.loans.eligibility.core.usecase;

import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityRequestDTO;
import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityResultDTO;
import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityStatus;
import ee.rsx.kata.bank.loans.eligibility.core.domain.CreditSegment;
import ee.rsx.kata.bank.loans.eligibility.core.domain.CreditSegmentType;
import ee.rsx.kata.bank.loans.eligibility.core.domain.FindCreditSegment;
import ee.rsx.kata.bank.loans.validation.LoadValidationLimits;
import ee.rsx.kata.bank.loans.validation.SsnValidationResultDTO;
import ee.rsx.kata.bank.loans.validation.ValidateSocialSecurityNumber;
import ee.rsx.kata.bank.loans.validation.ValidationLimitsDTO;
import ee.rsx.kata.bank.loans.validation.core.domain.SocialSecurityNumber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.util.List;
import java.util.Optional;

import static ee.rsx.kata.bank.loans.eligibility.LoanEligibilityStatus.*;
import static ee.rsx.kata.bank.loans.eligibility.core.domain.CreditSegmentType.*;
import static ee.rsx.kata.bank.loans.validation.SsnValidationResultDTO.*;
import static java.util.Arrays.stream;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

  @Mock
  private FindCreditSegment findCreditSegment;

  @InjectMocks
  private LoanEligibilityCalculation calculateLoanEligibility;

  @BeforeEach
  void setup() {
    when(validateSocialSecurityNumber.on(anyString()))
      .thenAnswer(okResultWithProvidedSsn());

    when(loadValidationLimits.invoke())
      .thenReturn(TEST_VALIDATION_LIMITS);

    when(findCreditSegment.forPerson(any()))
      .thenReturn(empty());
  }

  @Test
  @DisplayName("returns APPROVED result along with provided valid eligibility request data")
  void returns_APPROVED_result_with_providedValidEligibilityRequestData() {
    LoanEligibilityRequestDTO validRequest = testRequest().create();
    whenCreditSegmentFoundForPerson(DEFAULT_SSN, SEGMENT_3, 1000);

    LoanEligibilityResultDTO result = calculateLoanEligibility.on(validRequest);

    assertThat(result)
      .isEqualTo(expectedResult(APPROVED).eligibleLoanAmount(10_000).create());
  }

  @Test
  @DisplayName("returns DENIED result, when credit segment for given person is not found")
  void returns_DENIED_result_when_creditSegmentForGivenPerson_isNotFound() {
    LoanEligibilityRequestDTO validRequest = testRequest().create();
    whenCreditSegmentNotFoundForPerson(DEFAULT_SSN);

    LoanEligibilityResultDTO result = calculateLoanEligibility.on(validRequest);

    assertThat(result)
      .isEqualTo(expectedResult(DENIED).create());
  }

  @Test
  @DisplayName("returns DENIED result, when credit segment found, but its credit modifier too low for requested loan")
  void returns_DENIED_result_when_creditSegmentFound_butCreditModifierTooLowForRequestedLoan() {
    LoanEligibilityRequestDTO validRequest = testRequest().create();
    whenCreditSegmentFoundForPerson(DEFAULT_SSN, SEGMENT_3, 100);

    LoanEligibilityResultDTO result = calculateLoanEligibility.on(validRequest);

    assertThat(result)
      .isEqualTo(expectedResult(DENIED).eligibleLoanAmount(3599).create());
  }

  @Test
  @DisplayName("returns DENIED result, with no eligible amount when it would be less than minimum loan amount")
  void returns_DENIED_result_withNoEligibleAmount_whenItWouldBe_lessThan_MinimumLoanAmount() {
    LoanEligibilityRequestDTO validRequest = testRequest().amount(2000).period(20).create();
    whenCreditSegmentFoundForPerson(DEFAULT_SSN, SEGMENT_3, 100);

    LoanEligibilityResultDTO result = calculateLoanEligibility.on(validRequest);

    assertThat(result)
      .isEqualTo(expectedResult(DENIED).amount(2000).period(20).eligibleLoanAmount(null).create());
    //TODO test eligibility separately
  }

  @Test
  @DisplayName("returns APPROVED result, when low credit segment found, but period is long enough (46 months)")
  void returns_APPROVED_result_when_lowCreditSegmentFound_butPeriodIsLongEnough_fortySixMonths() {
    int fortySixMonths = 46;
    LoanEligibilityRequestDTO validRequest = testRequest().period(fortySixMonths).create();
    whenCreditSegmentFoundForPerson(DEFAULT_SSN, SEGMENT_1, 100);

    LoanEligibilityResultDTO result = calculateLoanEligibility.on(validRequest);

    assertThat(result)
      .isEqualTo(expectedResult(APPROVED).eligibleLoanAmount(4599).period(fortySixMonths).create());
    //TODO test eligibility separately
  }

  @Test
  @DisplayName("returns APPROVED result, when low credit segment found, but amount is small enough")
  void returns_APPROVED_result_when_lowCreditSegmentFound_butAmountIsSmallEnough() {
    int smallAmount = 2000;
    LoanEligibilityRequestDTO validRequest = testRequest().amount(smallAmount).create();
    whenCreditSegmentFoundForPerson(DEFAULT_SSN, SEGMENT_1, 60);

    LoanEligibilityResultDTO result = calculateLoanEligibility.on(validRequest);

    assertThat(result)
      .isEqualTo(expectedResult(APPROVED).eligibleLoanAmount(2159).amount(smallAmount).create());
  }

  @Nested
  @DisplayName("returns INVALID result")
  class ReturnsInvalidResult {

    @BeforeEach
    void setup() {
      reset(findCreditSegment);
    }

    @Test
    @DisplayName("with SSN error message, when invalid SSN provided")
    void with_ssnErrorMessage_whenInvalidSsnProvided() {
      String invalidSsn = "49002010966";
      whenSsnValidationFailsFor(invalidSsn);
      LoanEligibilityRequestDTO invalidRequest = testRequest().ssn(invalidSsn).create();

      LoanEligibilityResultDTO result = calculateLoanEligibility.on(invalidRequest);

      assertThat(result).isEqualTo(
        expectedResult(INVALID).ssn(invalidSsn).errors("SSN is not valid").create()
      );
    }

    private void whenSsnValidationFailsFor(String invalidSsn) {
      when(validateSocialSecurityNumber.on(invalidSsn)).thenReturn(invalidResultWith(invalidSsn));
    }

    @Test
    @DisplayName("with error message on too small loan amount, when too small loan amount provided")
    void with_loanAmountTooSmallMessage_whenTooSmallLoanAmountProvided() {
      Integer tooSmallLoanAmount = MINIMUM_REQUIRED_LOAN_AMOUNT - 1;
      LoanEligibilityRequestDTO invalidRequest = testRequest().amount(tooSmallLoanAmount).create();

      LoanEligibilityResultDTO result = calculateLoanEligibility.on(invalidRequest);

      assertThat(result).isEqualTo(
        expectedResult(INVALID).amount(tooSmallLoanAmount).errors("Loan amount is less than minimum required").create()
      );
    }

    @Test
    @DisplayName("with error message on too large loan amount, when too large loan amount provided")
    void with_loanAmountTooLargeMessage_whenTooLargeLoanAmountProvided() {
      Integer tooLargeLoanAmount = MAXIMUM_ALLOWED_LOAN_AMOUNT + 1;
      LoanEligibilityRequestDTO invalidRequest = testRequest().amount(tooLargeLoanAmount).create();

      LoanEligibilityResultDTO result = calculateLoanEligibility.on(invalidRequest);

      assertThat(result)
        .isEqualTo(
          expectedResult(INVALID).amount(tooLargeLoanAmount).errors("Loan amount is more than maximum allowed").create()
        );
    }

    @Test
    @DisplayName("with error message on too small loan period, when too small loan period provided")
    void with_loanPeriodTooSmallMessage_whenTooSmallLoanPeriodProvided() {
      Integer tooSmallLoanPeriod = MINIMUM_REQUIRED_LOAN_PERIOD - 1;
      LoanEligibilityRequestDTO invalidRequest = testRequest().period(tooSmallLoanPeriod).create();

      LoanEligibilityResultDTO result = calculateLoanEligibility.on(invalidRequest);

      assertThat(result)
        .isEqualTo(
          expectedResult(INVALID).period(tooSmallLoanPeriod).errors("Loan period is less than minimum required").create()
        );
    }

    @Test
    @DisplayName("with error message on too large loan period, when too big loan period provided")
    void with_loanPeriodTooLargeMessage_whenTooLargeLoanPeriodProvided() {
      Integer tooLargeLoanPeriod = MAXIMUM_ALLOWED_LOAN_PERIOD + 1;
      LoanEligibilityRequestDTO invalidRequest = testRequest().period(tooLargeLoanPeriod).create();

      LoanEligibilityResultDTO result = calculateLoanEligibility.on(invalidRequest);

      assertThat(result)
        .isEqualTo(
          expectedResult(INVALID).period(tooLargeLoanPeriod).errors("Loan period is more than maximum allowed").create()
        );
    }

    @Test
    @DisplayName("with several error messages, when eligibility result contains several invalid details")
    void with_severalErrorMessages_whenEligibilityResult_contains_severalInvalidDetails() {
      String invalidSsn = "49002010966";
      whenSsnValidationFailsFor(invalidSsn);
      Integer tooSmallLoanAmount = MINIMUM_REQUIRED_LOAN_AMOUNT - 1;
      Integer tooLargeLoanPeriod = MAXIMUM_ALLOWED_LOAN_PERIOD + 1;
      LoanEligibilityRequestDTO invalidRequest =
        testRequest().ssn(invalidSsn).amount(tooSmallLoanAmount).period(tooLargeLoanPeriod).create();

      LoanEligibilityResultDTO result = calculateLoanEligibility.on(invalidRequest);

      assertThat(result)
        .isEqualTo(
          expectedResult(INVALID)
            .ssn(invalidSsn)
            .amount(tooSmallLoanAmount)
            .period(tooLargeLoanPeriod)
            .errors(
              "SSN is not valid",
              "Loan amount is less than minimum required",
              "Loan period is more than maximum allowed"
            )
            .create()
        );
    }
  }

  private void whenCreditSegmentFoundForPerson(String withSsn, CreditSegmentType segmentType, int creditModifier) {
    SocialSecurityNumber ssn = new SocialSecurityNumber(withSsn);
    when(findCreditSegment.forPerson(ssn))
      .thenReturn(Optional.of(new CreditSegment(ssn, segmentType, creditModifier)));
  }

  private void whenCreditSegmentNotFoundForPerson(String withSsn) {
    SocialSecurityNumber ssn = new SocialSecurityNumber(withSsn);
    when(findCreditSegment.forPerson(ssn))
      .thenReturn(empty());
  }

  private static final String DEFAULT_SSN = "49002010965";
  private static final Integer DEFAULT_AMOUNT = 4500;
  private static final Integer DEFAULT_PERIOD = 36;

  private static DefaultTestRequest testRequest() {
    return new DefaultTestRequest();
  }

  static class DefaultTestRequest {

    private String ssn = DEFAULT_SSN;
    private Integer amount = DEFAULT_AMOUNT;
    private Integer period = DEFAULT_PERIOD;

    public DefaultTestRequest ssn(String value) {
      this.ssn = value;
      return this;
    }

    public DefaultTestRequest amount(Integer value) {
      this.amount = value;
      return this;
    }

    public DefaultTestRequest period(Integer value) {
      this.period = value;
      return this;
    }

    public LoanEligibilityRequestDTO create() {
      return new LoanEligibilityRequestDTO(this.ssn, this.amount, this.period);
    }
  }

  private static DefaultTestResult expectedResult() {
    return new DefaultTestResult();
  }

  private static DefaultTestResult expectedResult(LoanEligibilityStatus withStatus) {
    return expectedResult().status(withStatus);
  }

  static class DefaultTestResult {

    private LoanEligibilityStatus status = APPROVED;
    private List<String> errors = null;
    private String ssn = DEFAULT_SSN;
    private Integer amount = DEFAULT_AMOUNT;
    private Integer period = DEFAULT_PERIOD;
    private Integer eligibleLoanAmount = null;

    public DefaultTestResult status(LoanEligibilityStatus value) {
      this.status = value;
      return this;
    }

    public DefaultTestResult errors(String... errors) {
      this.errors = stream(errors).toList();
      return this;
    }

    public DefaultTestResult ssn(String value) {
      this.ssn = value;
      return this;
    }

    public DefaultTestResult amount(Integer value) {
      this.amount = value;
      return this;
    }

    public DefaultTestResult period(Integer value) {
      this.period = value;
      return this;
    }

    public DefaultTestResult eligibleLoanAmount(Integer value) {
      this.eligibleLoanAmount = value;
      return this;
    }

    public LoanEligibilityResultDTO create() {
      return new LoanEligibilityResultDTO(
        status, errors, ssn, amount, period, eligibleLoanAmount
      );
    }
  }
}
