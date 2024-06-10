package ee.rsx.kata.bank.loans.eligibility.core;

import ee.rsx.kata.bank.loans.eligibility.CalculateLoanEligibility;
import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityRequestDTO;
import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityResultDTO;
import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityStatus;
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
import static ee.rsx.kata.bank.loans.validation.ValidationStatus.OK;
import static java.util.Optional.*;
import static org.springframework.util.CollectionUtils.isEmpty;

@Named
@RequiredArgsConstructor
class LoanEligibilityCalculation implements CalculateLoanEligibility {

  private final ValidateSocialSecurityNumber validateSocialSecurityNumber;
  private final LoadValidationLimits loadValidationLimits;
  private final FindCreditSegment findCreditSegment;

  @Override
  public LoanEligibilityResultDTO on(LoanEligibilityRequestDTO eligibilityRequest) {
    List<String> validationErrors = validate(eligibilityRequest);

    LoanEligibilityStatus result = isEmpty(validationErrors)
      ? calculateEligibility(eligibilityRequest)
      : INVALID;

    return new LoanEligibilityResultDTO(
      result,
      validationErrors,
      eligibilityRequest.ssn(),
      eligibilityRequest.loanAmount(),
      eligibilityRequest.loanPeriodMonths()
    );
  }

  private LoanEligibilityStatus calculateEligibility(LoanEligibilityRequestDTO request) {
    SocialSecurityNumber ssn = new SocialSecurityNumber(request.ssn());

    Optional<CreditSegment> creditSegment = findCreditSegment.forPerson(ssn);

    return creditSegment
      .map(segment -> {
        double creditScore = (double) segment.creditModifier() / request.loanAmount() * request.loanPeriodMonths();
        return creditScore > 1 ? APPROVED : DENIED;
      })
      .orElse(DENIED);
  }

  private @Nullable List<String> validate(LoanEligibilityRequestDTO eligibilityRequest) {
    var limits = loadValidationLimits.invoke();

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
}
