package ee.rsx.kata.bank.loans.usecases;

import ee.rsx.kata.bank.loans.api.eligibility.CalculateLoanEligibility;
import ee.rsx.kata.bank.loans.api.eligibility.LoanEligibilityRequestDTO;
import ee.rsx.kata.bank.loans.api.eligibility.LoanEligibilityResultDTO;
import ee.rsx.kata.bank.loans.api.eligibility.LoanEligibilityStatus;
import ee.rsx.kata.bank.loans.api.validation.limits.LoadValidationLimits;
import ee.rsx.kata.bank.loans.api.validation.limits.ValidationLimitsDTO;
import ee.rsx.kata.bank.loans.api.validation.ssn.ValidateSocialSecurityNumber;
import ee.rsx.kata.bank.loans.domain.DetermineEligiblePeriod;
import ee.rsx.kata.bank.loans.domain.LoanEligibility;
import ee.rsx.kata.bank.loans.domain.segment.CreditSegment;
import ee.rsx.kata.bank.loans.domain.segment.FindCreditSegment;
import ee.rsx.kata.bank.loans.domain.ssn.SocialSecurityNumber;
import jakarta.annotation.Nullable;
import jakarta.inject.Named;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static ee.rsx.kata.bank.loans.api.eligibility.LoanEligibilityStatus.*;
import static ee.rsx.kata.bank.loans.api.validation.ssn.ValidationStatus.OK;
import static ee.rsx.kata.bank.loans.domain.segment.CreditSegmentType.DEBT;
import static java.util.Objects.*;
import static java.util.Optional.*;
import static org.springframework.util.CollectionUtils.isEmpty;

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

  private @Nullable List<String> validate(LoanEligibilityRequestDTO eligibilityRequest, ValidationLimitsDTO limits) {
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
    var amount = request.loanAmount();
    if (amount < limits.minimumLoanAmount()) {
      return of("Loan amount is less than minimum required");
    }
    if (amount > limits.maximumLoanAmount()) {
      return of("Loan amount is more than maximum allowed");
    }
    return empty();
  }

  private Optional<String> checkForPeriodErrorIn(LoanEligibilityRequestDTO request, ValidationLimitsDTO limits) {
    var period = request.loanPeriodMonths();
    if (period < limits.minimumLoanPeriodMonths()) {
      return of("Loan period is less than minimum required");
    }
    if (period > limits.maximumLoanPeriodMonths()) {
      return of("Loan period is more than maximum allowed");
    }
    return empty();
  }

  private LoanEligibility calculateEligibility(LoanEligibilityRequestDTO request, ValidationLimitsDTO limits) {
    var ssn = new SocialSecurityNumber(request.ssn());
    Optional<CreditSegment> creditSegment = findCreditSegment.forPerson(ssn);

    var status = creditSegment.map(segment -> determineEligibilityStatusFor(request, segment))
      .orElse(DENIED);

    var amount = creditSegment.map(segment -> determineEligibleAmountFor(limits, segment, request.loanPeriodMonths()))
      .orElse(null);

    final var initialAmount = amount;
    Integer newPeriod = creditSegment
      .filter(segment -> segment.type() != DEBT && isNull(initialAmount))
      .flatMap(segment -> determineEligiblePeriod.forLoan(request, segment))
      .filter(calculatedPeriod ->
        limits.minimumLoanPeriodMonths() <= calculatedPeriod && calculatedPeriod <= limits.maximumLoanPeriodMonths()
      )
      .orElse(null);

    if (nonNull(newPeriod)) {
      amount = creditSegment.map(segment -> determineEligibleAmountFor(limits, segment, newPeriod))
        .orElse(null);
    }

    return new LoanEligibility(status, amount, newPeriod);
  }

  private LoanEligibilityStatus determineEligibilityStatusFor(LoanEligibilityRequestDTO request, CreditSegment creditSegment) {
    double creditScore = (double) creditSegment.creditModifier() / request.loanAmount() * request.loanPeriodMonths();
    return creditScore > 1 ? APPROVED : DENIED;
  }

  private Integer determineEligibleAmountFor(ValidationLimitsDTO limits, CreditSegment creditSegment, Integer loanPeriodMonths) {
    int eligibleAmount = Math.min(
      limits.maximumLoanAmount(),
      creditSegment.creditModifier() * loanPeriodMonths - 1
    );

    return eligibleAmount >= limits.minimumLoanAmount() ? eligibleAmount : null;
  }
}
