package ee.rsx.kata.bank.loans.eligibility.core.usecase;

import ee.rsx.kata.bank.loans.eligibility.CalculateLoanEligibility;
import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityRequestDTO;
import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityResultDTO;
import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityStatus;
import ee.rsx.kata.bank.loans.eligibility.core.domain.CreditSegment;
import ee.rsx.kata.bank.loans.eligibility.core.domain.DetermineEligiblePeriod;
import ee.rsx.kata.bank.loans.eligibility.core.domain.FindCreditSegment;
import ee.rsx.kata.bank.loans.eligibility.core.domain.LoanEligibility;
import ee.rsx.kata.bank.loans.validation.LoadValidationLimits;
import ee.rsx.kata.bank.loans.validation.ValidateSocialSecurityNumber;
import ee.rsx.kata.bank.loans.validation.ValidationLimitsDTO;
import ee.rsx.kata.bank.loans.validation.core.domain.SocialSecurityNumber;
import jakarta.annotation.Nullable;
import jakarta.inject.Named;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static ee.rsx.kata.bank.loans.eligibility.LoanEligibilityStatus.*;
import static ee.rsx.kata.bank.loans.eligibility.core.domain.CreditSegmentType.DEBT;
import static ee.rsx.kata.bank.loans.validation.ValidationStatus.OK;
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
