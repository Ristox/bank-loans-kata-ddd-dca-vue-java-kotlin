package ee.rsx.kata.bank.loans.usecases

import ee.rsx.kata.bank.loans.domain.limits.gateway.DetermineEligiblePeriod
import ee.rsx.kata.bank.loans.domain.segment.CreditSegment
import ee.rsx.kata.bank.loans.domain.segment.CreditSegmentType
import ee.rsx.kata.bank.loans.domain.segment.CreditSegmentType.SEGMENT_1
import ee.rsx.kata.bank.loans.domain.segment.CreditSegmentType.SEGMENT_3
import ee.rsx.kata.bank.loans.domain.segment.gateway.FindCreditSegment
import ee.rsx.kata.bank.loans.domain.ssn.SocialSecurityNumber
import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityRequestDTO
import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityResultDTO
import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityStatus
import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityStatus.APPROVED
import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityStatus.DENIED
import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityStatus.INVALID
import ee.rsx.kata.bank.loans.validation.limits.LoadValidationLimits
import ee.rsx.kata.bank.loans.validation.limits.ValidationLimitsDTO
import ee.rsx.kata.bank.loans.validation.ssn.SsnValidationResultDTO.Companion.invalidResultWith
import ee.rsx.kata.bank.loans.validation.ssn.SsnValidationResultDTO.Companion.okResultWith
import ee.rsx.kata.bank.loans.validation.ssn.ValidateSocialSecurityNumber
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.reset
import org.mockito.Mockito.`when`
import org.mockito.invocation.InvocationOnMock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.mockito.stubbing.Answer
import java.util.*

@DisplayName("Loan eligibility calculation")
@ExtendWith(MockitoExtension::class)
internal class LoanEligibilityCalculationTestKotlin {

  @Mock
  private lateinit var validateSocialSecurityNumber: ValidateSocialSecurityNumber

  @Mock
  private lateinit var loadValidationLimits: LoadValidationLimits

  @Mock
  private lateinit var findCreditSegment: FindCreditSegment

  @Mock
  private lateinit var determineEligiblePeriod: DetermineEligiblePeriod

  @InjectMocks
  private lateinit var calculateLoanEligibility: LoanEligibilityCalculation

  @BeforeEach
  fun setup() {
    whenever(validateSocialSecurityNumber(any()))
      .thenAnswer(okResultWithProvidedSsn())
    whenever(loadValidationLimits())
      .thenReturn(TEST_VALIDATION_LIMITS)
    whenever(findCreditSegment(any()))
      .thenReturn(Optional.empty())
  }

  @Nested
  @DisplayName("returns DENIED result")
  internal inner class ReturnsDeniedResult {

    @Test
    fun `when credit segment for given person is not found`() {
      val validRequest = testRequest().create()
      whenCreditSegmentNotFoundForPerson(DEFAULT_SSN)

      val result = calculateLoanEligibility.invoke(validRequest)

      assertThat(result)
        .isEqualTo(
          expectedResult(withStatus = DENIED).create()
        )
    }

    @Test
    fun `when credit segment found, but its credit modifier too low for requested loan`() {
      val validRequest = testRequest().create()
      val tooLowCreditModifier = 100
      whenCreditSegmentFoundForPerson(DEFAULT_SSN, SEGMENT_3, tooLowCreditModifier)

      val result = calculateLoanEligibility.invoke(validRequest)

      assertThat(result)
        .isEqualTo(
          expectedResult(withStatus = DENIED)
            .eligibleLoanAmount(3599)
            .create()
        )
    }

    @Test
    fun `with no eligible amount when it would be less than minimum loan amount`() {
      val validRequest = testRequest().amount(2000).period(20).create()
      whenCreditSegmentFoundForPerson(DEFAULT_SSN, SEGMENT_3, 100)

      val result = calculateLoanEligibility.invoke(validRequest)

      assertThat(result)
        .isEqualTo(
          expectedResult(withStatus = DENIED)
            .amount(2000)
            .period(20)
            .eligibleLoanAmount(null)
            .create()
        )
    }

    @Test
    fun `with new eligible period (received from gateway) and amount, when no amount available for given period`() {
      val shortPeriodOneYear = 12
      val validRequest = testRequest().amount(5000).period(shortPeriodOneYear).create()
      val creditModifier = 100
      val segment = whenCreditSegmentFoundForPerson(DEFAULT_SSN, SEGMENT_3, creditModifier)
      val newEligiblePeriod = whenNewEligiblePeriodDeterminedFor(validRequest, segment, 51)

      val result = calculateLoanEligibility.invoke(validRequest)

      val expectedNewEligibleLoanAmount = newEligiblePeriod * creditModifier - 1
      assertThat(result)
        .isEqualTo(
          expectedResult(withStatus = DENIED)
            .amount(5000)
            .period(shortPeriodOneYear)
            .eligibleLoanAmount(expectedNewEligibleLoanAmount)
            .eligibleLoanPeriod(newEligiblePeriod)
            .create()
        )
    }

    @Test
    @DisplayName("with no new eligible period (received from gateway) nor amount, when new determined period above maximum")
    fun `with no new eligible period (received from gateway) nor amount, when new determined period above maximum`() {
      val shortPeriodOneYear = 12
      val validRequest = testRequest().amount(9000).period(shortPeriodOneYear).create()
      val creditModifier = 100
      val segment = whenCreditSegmentFoundForPerson(DEFAULT_SSN, SEGMENT_3, creditModifier)
      whenNewEligiblePeriodDeterminedFor(validRequest, segment, 91)

      val result = calculateLoanEligibility.invoke(validRequest)

      assertThat(result)
        .isEqualTo(
          expectedResult(withStatus = DENIED)
            .amount(9000)
            .period(shortPeriodOneYear)
            .eligibleLoanAmount(null)
            .eligibleLoanPeriod(null)
            .create()
        )
    }

    private fun whenNewEligiblePeriodDeterminedFor(
      validRequest: LoanEligibilityRequestDTO, segment: CreditSegment, newEligiblePeriod: Int
    ): Int {
      `when`(determineEligiblePeriod(forAmount = validRequest.loanAmount, forSegment = segment))
        .thenReturn(Optional.of(newEligiblePeriod))
      return newEligiblePeriod
    }
  }

  @Nested
  @DisplayName("returns APPROVED result")
  internal inner class ReturnsApprovedResult {

    @Test
    fun `along with provided valid eligibility request data`() {
      val validRequest = testRequest().create()
      whenCreditSegmentFoundForPerson(DEFAULT_SSN, SEGMENT_3, 1000)

      val result = calculateLoanEligibility.invoke(validRequest)

      assertThat(result)
        .isEqualTo(
          expectedResult(withStatus = APPROVED)
            .eligibleLoanAmount(10000)
            .create()
        )
    }

    @Test
    fun `when low credit segment found, but period is long enough (46 months)`() {
      val fortySixMonths = 46
      val validRequest = testRequest().period(fortySixMonths).create()

      whenCreditSegmentFoundForPerson(DEFAULT_SSN, SEGMENT_1, 100)

      val result = calculateLoanEligibility.invoke(validRequest)
      assertThat(result)
        .isEqualTo(
          expectedResult(APPROVED)
            .eligibleLoanAmount(4599)
            .period(fortySixMonths)
            .create()
        )
    }

    @Test
    fun `when low credit segment found, but amount is small enough`() {
      val smallAmount = 2000
      val validRequest = testRequest().amount(smallAmount).create()
      whenCreditSegmentFoundForPerson(DEFAULT_SSN, SEGMENT_1, 60)

      val result = calculateLoanEligibility.invoke(validRequest)

      assertThat(result)
        .isEqualTo(
          expectedResult(withStatus = APPROVED)
            .eligibleLoanAmount(2159)
            .amount(smallAmount)
            .create()
        )
    }
  }

  @Nested
  @DisplayName("returns INVALID result")
  internal inner class ReturnsInvalidResult {
    @BeforeEach
    fun setup() {
      reset(findCreditSegment)
    }

    @Test
    fun `with SSN error message, when invalid SSN provided`() {
      val invalidSsn = "49002010966"
      whenSsnValidationFailsFor(invalidSsn)
      val invalidRequest = testRequest().ssn(invalidSsn).create()

      val result = calculateLoanEligibility.invoke(invalidRequest)

      assertThat(result).isEqualTo(
        expectedResult(withStatus = INVALID)
          .ssn(invalidSsn)
          .errors("SSN is not valid")
          .create()
      )
    }

    private fun whenSsnValidationFailsFor(invalidSsn: String) {
      `when`(validateSocialSecurityNumber(invalidSsn))
        .thenReturn(invalidResultWith(invalidSsn))
    }

    @Test
    fun `with error message on too small loan amount, when too small loan amount provided`() {
      val tooSmallLoanAmount = MINIMUM_REQUIRED_LOAN_AMOUNT - 1
      val invalidRequest = testRequest().amount(tooSmallLoanAmount).create()

      val result = calculateLoanEligibility.invoke(invalidRequest)

      assertThat(result).isEqualTo(
        expectedResult(withStatus = INVALID).amount(tooSmallLoanAmount)
          .errors("Loan amount is less than minimum required")
          .create()
      )
    }

    @Test
    fun `with error message on too large loan amount, when too large loan amount provided`() {
      val tooLargeLoanAmount = MAXIMUM_ALLOWED_LOAN_AMOUNT + 1
      val invalidRequest = testRequest().amount(tooLargeLoanAmount).create()

      val result = calculateLoanEligibility.invoke(invalidRequest)

      assertThat(result)
        .isEqualTo(
          expectedResult(withStatus = INVALID)
            .amount(tooLargeLoanAmount)
            .errors("Loan amount is more than maximum allowed")
            .create()
        )
    }

    @Test
    @DisplayName("with error message on too small loan period, when too small loan period provided")
    fun `with error message on too small loan period, when too small loan period provided`() {
      val tooSmallLoanPeriod = MINIMUM_REQUIRED_LOAN_PERIOD - 1
      val invalidRequest = testRequest().period(tooSmallLoanPeriod).create()

      val result = calculateLoanEligibility.invoke(invalidRequest)

      assertThat(result)
        .isEqualTo(
          expectedResult(withStatus = INVALID)
            .period(tooSmallLoanPeriod)
            .errors("Loan period is less than minimum required")
            .create()
        )
    }

    @Test
    fun `with error message on too large loan period, when too big loan period provided`() {
      val tooLargeLoanPeriod = MAXIMUM_ALLOWED_LOAN_PERIOD + 1
      val invalidRequest = testRequest().period(tooLargeLoanPeriod).create()

      val result = calculateLoanEligibility.invoke(invalidRequest)

      assertThat(result)
        .isEqualTo(
          expectedResult(withStatus = INVALID)
            .period(tooLargeLoanPeriod)
            .errors("Loan period is more than maximum allowed")
            .create()
        )
    }

    @Test
    fun `with several error messages, when eligibility result contains several invalid details`() {
      val invalidSsn = "49002010966"
      whenSsnValidationFailsFor(invalidSsn)
      val tooSmallLoanAmount = MINIMUM_REQUIRED_LOAN_AMOUNT - 1
      val tooLargeLoanPeriod = MAXIMUM_ALLOWED_LOAN_PERIOD + 1
      val invalidRequest = testRequest().ssn(invalidSsn).amount(tooSmallLoanAmount).period(tooLargeLoanPeriod).create()

      val result = calculateLoanEligibility.invoke(invalidRequest)

      assertThat(result)
        .isEqualTo(
          expectedResult(withStatus = INVALID)
            .ssn(invalidSsn)
            .amount(tooSmallLoanAmount)
            .period(tooLargeLoanPeriod)
            .errors(
              "SSN is not valid",
              "Loan amount is less than minimum required",
              "Loan period is more than maximum allowed"
            )
            .create()
        )
    }
  }

  private fun whenCreditSegmentFoundForPerson(withSsn: String, segmentType: CreditSegmentType, creditModifier: Int): CreditSegment {
    val ssn = SocialSecurityNumber(withSsn)
    val foundSegment = CreditSegment(ssn, segmentType, creditModifier)
    `when`(findCreditSegment(ssn))
      .thenReturn(Optional.of(foundSegment))
    return foundSegment
  }

  private fun whenCreditSegmentNotFoundForPerson(withSsn: String) {
    val ssn = SocialSecurityNumber(withSsn)
    `when`(findCreditSegment(ssn))
      .thenReturn(Optional.empty())
  }

  internal class DefaultTestRequest {
    private var ssn = DEFAULT_SSN
    private var amount = DEFAULT_AMOUNT
    private var period = DEFAULT_PERIOD
    fun ssn(value: String): DefaultTestRequest {
      ssn = value
      return this
    }

    fun amount(value: Int): DefaultTestRequest {
      amount = value
      return this
    }

    fun period(value: Int): DefaultTestRequest {
      period = value
      return this
    }

    fun create(): LoanEligibilityRequestDTO {
      return LoanEligibilityRequestDTO(ssn, amount, period)
    }
  }

  internal class DefaultTestResult {
    private var status = APPROVED
    private var errors: List<String>? = null
    private var ssn = DEFAULT_SSN
    private var amount = DEFAULT_AMOUNT
    private var period = DEFAULT_PERIOD
    private var eligibleLoanAmount: Int? = null
    private var eligibleLoanPeriod: Int? = null
    fun status(value: LoanEligibilityStatus): DefaultTestResult {
      status = value
      return this
    }

    fun errors(vararg errors: String): DefaultTestResult {
      this.errors = Arrays.stream(errors).toList()
      return this
    }

    fun ssn(value: String): DefaultTestResult {
      ssn = value
      return this
    }

    fun amount(value: Int): DefaultTestResult {
      amount = value
      return this
    }

    fun period(value: Int): DefaultTestResult {
      period = value
      return this
    }

    fun eligibleLoanAmount(value: Int?): DefaultTestResult {
      eligibleLoanAmount = value
      return this
    }

    fun eligibleLoanPeriod(value: Int?): DefaultTestResult {
      eligibleLoanPeriod = value
      return this
    }

    fun create(): LoanEligibilityResultDTO {
      return LoanEligibilityResultDTO(
        status, errors, ssn, amount, period, eligibleLoanAmount, eligibleLoanPeriod
      )
    }
  }

  companion object {
    private const val MINIMUM_REQUIRED_LOAN_AMOUNT = 2000
    private const val MAXIMUM_ALLOWED_LOAN_AMOUNT = 10000
    private const val MINIMUM_REQUIRED_LOAN_PERIOD = 12
    private const val MAXIMUM_ALLOWED_LOAN_PERIOD = 60

    private val TEST_VALIDATION_LIMITS = ValidationLimitsDTO(
      MINIMUM_REQUIRED_LOAN_AMOUNT,
      MAXIMUM_ALLOWED_LOAN_AMOUNT,
      MINIMUM_REQUIRED_LOAN_PERIOD,
      MAXIMUM_ALLOWED_LOAN_PERIOD
    )

    private const val DEFAULT_SSN = "49002010965"
    private const val DEFAULT_AMOUNT = 4500
    private const val DEFAULT_PERIOD = 36

    private fun okResultWithProvidedSsn() =
      Answer { methodCall: InvocationOnMock ->
        val providedSsn = methodCall.getArgument<String>(0)
        okResultWith(providedSsn)
      }

    private fun testRequest() = DefaultTestRequest()

    private fun expectedResult() = DefaultTestResult()

    private fun expectedResult(withStatus: LoanEligibilityStatus) = expectedResult().status(withStatus)
  }
}
