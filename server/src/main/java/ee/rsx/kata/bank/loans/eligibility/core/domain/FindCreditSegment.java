package ee.rsx.kata.bank.loans.eligibility.core.domain;

import ee.rsx.kata.bank.loans.validation.core.domain.SocialSecurityNumber;

import java.util.Optional;

@FunctionalInterface
public interface FindCreditSegment {

  Optional<CreditSegment> forPerson(SocialSecurityNumber ssn);
}
