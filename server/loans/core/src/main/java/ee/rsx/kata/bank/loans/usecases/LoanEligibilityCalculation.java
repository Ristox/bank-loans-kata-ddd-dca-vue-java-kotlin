package ee.rsx.kata.bank.loans.usecases;

import ee.rsx.kata.bank.loans.domain.loan.LoanEligibility;
import ee.rsx.kata.bank.loans.domain.limits.gateway.DetermineEligiblePeriod;
import ee.rsx.kata.bank.loans.domain.loan.Applicant;
import ee.rsx.kata.bank.loans.domain.loan.Loan;
import ee.rsx.kata.bank.loans.domain.segment.CreditSegment;
import ee.rsx.kata.bank.loans.domain.segment.gateway.FindCreditSegment;
import ee.rsx.kata.bank.loans.domain.ssn.SocialSecurityNumber;
import ee.rsx.kata.bank.loans.eligibility.CalculateLoanEligibility;
import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityRequestDTO;
import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityResultDTO;
import ee.rsx.kata.bank.loans.validation.limits.LoadValidationLimits;
import ee.rsx.kata.bank.loans.validation.limits.ValidationLimitsDTO;
import ee.rsx.kata.bank.loans.validation.ssn.ValidateSocialSecurityNumber;
import jakarta.annotation.Nullable;
import jakarta.inject.Named;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static ee.rsx.kata.bank.loans.eligibility.LoanEligibilityStatus.*;
import static ee.rsx.kata.bank.loans.validation.ssn.ValidationStatus.OK;
import static java.util.Optional.*;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

@Named
@RequiredArgsConstructor
class LoanEligibilityCalculation implements CalculateLoanEligibility {

  private final ValidateSocialSecurityNumber validateSocialSecurityNumber;
  private final LoadValidationLimits loadValidationLimits;
  private final FindCreditSegment findCreditSegment;
  private final DetermineEligiblePeriod determineEligiblePeriod;

  @Override
  public LoanEligibilityResultDTO on(LoanEligibilityRequestDTO eligibilityRequest) {
    var limits = loadValidationLimits.invoke();
    List<String> validationErrors = validate(eligibilityRequest, limits);

    var eligibility = isEmpty(validationErrors)
      ? calculateEligibility(eligibilityRequest, limits)
      : new LoanEligibility(INVALID, null, null);

    return new LoanEligibilityResultDTO(
      eligibility.status(),
      validationErrors,
      eligibilityRequest.ssn(),
      eligibilityRequest.loanAmount(),
      eligibilityRequest.loanPeriodMonths(),
      eligibility.eligibleAmount(),
      eligibility.eligiblePeriod()
    );
  }

  private @Nullable List<String> validate(
    LoanEligibilityRequestDTO eligibilityRequest,
    ValidationLimitsDTO limits
  ) {
    List<String> errors = Stream.of(
        checkForSsnErrorIn(eligibilityRequest),
        checkForAmountErrorIn(eligibilityRequest, limits),
        checkForPeriodErrorIn(eligibilityRequest, limits)
      )
      .flatMap(Optional::stream)
      .toList();


    return errors.isEmpty() ? null : errors;
  }

  private Optional<String> checkForSsnErrorIn(LoanEligibilityRequestDTO request) {
    var ssnValidity = validateSocialSecurityNumber.on(request.ssn()).status();

    return ssnValidity == OK ? empty() : of("SSN is not valid");
  }

  private Optional<String> checkForAmountErrorIn(LoanEligibilityRequestDTO request, ValidationLimitsDTO limits) {
    return switch (request.loanAmount()) {
      case Integer amount when amount < limits.minimumLoanAmount() -> of("Loan amount is less than minimum required");
      case Integer amount when amount > limits.maximumLoanAmount() -> of("Loan amount is more than maximum allowed");
      default -> empty();
    };
  }

  private Optional<String> checkForPeriodErrorIn(LoanEligibilityRequestDTO request, ValidationLimitsDTO limits) {
    return switch (request.loanPeriodMonths()) {
      case Integer period when period < limits.minimumLoanPeriodMonths() -> of("Loan period is less than minimum required");
      case Integer period when period > limits.maximumLoanPeriodMonths() -> of("Loan period is more than maximum allowed");
      default -> empty();
    };
  }

  private LoanEligibility calculateEligibility(LoanEligibilityRequestDTO request, ValidationLimitsDTO limits) {
    var ssn = new SocialSecurityNumber(request.ssn());
    var amount = request.loanAmount();
    var period = request.loanPeriodMonths();

    return findCreditSegment.forPerson(ssn)
      .map(segment -> applicantWith(ssn, segment, amount, limits, period))
      .filter(Applicant::isNotInDebt)
      .map(applicant ->
          applicant.attemptFindingEligibilityWithAmount().orElseGet(() -> recalculateEligibilityFor(applicant, limits)
        ))
      .orElseGet(
        () -> new LoanEligibility(DENIED, null, null)
      );
  }

  private static Applicant applicantWith(
    SocialSecurityNumber ssn, CreditSegment segment, Integer amount, ValidationLimitsDTO limits, Integer period
  ) {
    var loan = Loan.with(amount, period, limits.minimumLoanAmount(), limits.maximumLoanAmount());
    return Applicant.with(ssn, segment, loan);
  }

  private LoanEligibility recalculateEligibilityFor(Applicant applicant, ValidationLimitsDTO limits) {
    Optional<Integer> newPeriod =
      determineEligiblePeriod.forLoan(applicant.loan().amount(), applicant.segment())
        .filter(period ->
          limits.minimumLoanPeriodMonths() <= period && period <= limits.maximumLoanPeriodMonths()
        );

    return newPeriod.map(applicant::eligibilityForPeriod)
      .orElseGet(applicant::eligibility);
  }
}
