package ee.rsx.kata.bank.loans.usecases;

import ee.rsx.kata.bank.loans.domain.LoanEligibility;
import ee.rsx.kata.bank.loans.domain.limits.gateway.DetermineEligiblePeriod;
import ee.rsx.kata.bank.loans.domain.segment.CreditSegment;
import ee.rsx.kata.bank.loans.domain.segment.gateway.FindCreditSegment;
import ee.rsx.kata.bank.loans.domain.ssn.SocialSecurityNumber;
import ee.rsx.kata.bank.loans.eligibility.CalculateLoanEligibility;
import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityRequestDTO;
import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityResultDTO;
import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityStatus;
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

    return creditSegment
      .map(segment -> {
        var status = determineEligibilityStatusFor(request, segment);

        return determineEligibleAmountFor(limits, segment, request.loanPeriodMonths())
          .map(eligibleAmount -> new LoanEligibility(status, eligibleAmount, null))
          .orElseGet(
            () -> recalculateEligibilityFor(request, limits, segment, status)
          );
      })
      .orElseGet(
        () -> new LoanEligibility(DENIED, null, null)
      );
  }

  private LoanEligibility recalculateEligibilityFor(
    LoanEligibilityRequestDTO request,
    ValidationLimitsDTO limits,
    CreditSegment segment,
    LoanEligibilityStatus status
  ) {
    if (segment.isDebt()) {
      return new LoanEligibility(status, null, null);
    }
    Optional<Integer> newPeriod =
      determineEligiblePeriod.forLoan(request.loanAmount(), segment)
        .filter(period ->
          limits.minimumLoanPeriodMonths() <= period && period <= limits.maximumLoanPeriodMonths()
        );

    Optional<Integer> newAmount =
      newPeriod.flatMap(period -> determineEligibleAmountFor(limits, segment, period));

    return new LoanEligibility(status, newAmount.orElse(null), newPeriod.orElse(null));
  }

  private LoanEligibilityStatus determineEligibilityStatusFor(
    LoanEligibilityRequestDTO request, CreditSegment creditSegment
  ) {
    double creditScore =
      (double) creditSegment.getCreditModifier() / request.loanAmount() * request.loanPeriodMonths();

    return (!creditSegment.isDebt() && creditScore > 1) ? APPROVED : DENIED;
  }

  private Optional<Integer> determineEligibleAmountFor(
    ValidationLimitsDTO limits, CreditSegment creditSegment, Integer loanPeriodMonths
  ) {
    int eligibleAmount = Math.min(
      limits.maximumLoanAmount(),
      creditSegment.getCreditModifier() * loanPeriodMonths - 1
    );

    return eligibleAmount >= limits.minimumLoanAmount() ? of(eligibleAmount) : empty();
  }
}
