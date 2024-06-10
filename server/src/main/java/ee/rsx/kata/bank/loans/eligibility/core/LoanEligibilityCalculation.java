package ee.rsx.kata.bank.loans.eligibility.core;

import ee.rsx.kata.bank.loans.eligibility.CalculateLoanEligibility;
import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityRequestDTO;
import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityResultDTO;
import ee.rsx.kata.bank.loans.eligibility.LoanEligibilityStatus;
import ee.rsx.kata.bank.loans.validation.LoadValidationLimits;
import ee.rsx.kata.bank.loans.validation.ValidateSocialSecurityNumber;
import ee.rsx.kata.bank.loans.validation.ValidationLimitsDTO;
import ee.rsx.kata.bank.loans.validation.ValidationStatus;
import jakarta.inject.Named;

import java.util.ArrayList;
import java.util.List;

import static ee.rsx.kata.bank.loans.eligibility.LoanEligibilityStatus.*;
import static ee.rsx.kata.bank.loans.validation.ValidationStatus.OK;
import static org.springframework.util.CollectionUtils.isEmpty;

@Named
class LoanEligibilityCalculation implements CalculateLoanEligibility {

  private final ValidateSocialSecurityNumber validateSocialSecurityNumber;
  private final LoadValidationLimits loadValidationLimits;

  LoanEligibilityCalculation(
    ValidateSocialSecurityNumber validateSocialSecurityNumber,
    LoadValidationLimits loadValidationLimits
  ) {
    this.validateSocialSecurityNumber = validateSocialSecurityNumber;
    this.loadValidationLimits = loadValidationLimits;
  }

  @Override
  public LoanEligibilityResultDTO on(LoanEligibilityRequestDTO eligibilityRequest) {
    List<String> validationErrors = validate(eligibilityRequest);

    LoanEligibilityStatus result = isEmpty(validationErrors) ? APPROVED : INVALID;

    return new LoanEligibilityResultDTO(
      result,
      validationErrors,
      eligibilityRequest.ssn(),
      eligibilityRequest.loanAmount(),
      eligibilityRequest.loanPeriodMonths()
    );
  }

  private List<String> validate(LoanEligibilityRequestDTO eligibilityRequest) {
    List<String> errors = new ArrayList<>();

    ValidationStatus ssnValidity = validateSocialSecurityNumber.on(eligibilityRequest.ssn()).status();
    if (ssnValidity != OK) {
      errors.add("SSN is not valid");
    }

    ValidationLimitsDTO limits = loadValidationLimits.invoke();

    Integer amount = eligibilityRequest.loanAmount();
    if (amount < limits.minimumLoanAmount()) {
      errors.add("Loan amount is less than minimum required");
    } else if (amount > limits.maximumLoanAmount()) {
      errors.add("Loan amount is more than maximum allowed");
    }

    Integer period = eligibilityRequest.loanPeriodMonths();
    if (period < limits.minimumLoanPeriodMonths()) {
      errors.add("Loan period is less than minimum required");
    } else if (period > limits.maximumLoanPeriodMonths()) {
      errors.add("Loan period is more than maximum allowed");
    }

    return errors.isEmpty() ? null : errors;
  }
}
