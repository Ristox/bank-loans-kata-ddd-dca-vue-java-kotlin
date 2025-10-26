package ee.rsx.kata.bank.loans.adapter.eligibility.db;

import ee.rsx.kata.bank.loans.adapter.eligibility.db.mapper.CreditSegmentMapper;
import ee.rsx.kata.bank.loans.adapter.eligibility.db.repository.CreditSegmentRepository;
import ee.rsx.kata.bank.loans.domain.segment.CreditSegment;
import ee.rsx.kata.bank.loans.domain.segment.gateway.FindCreditSegment;
import ee.rsx.kata.bank.loans.domain.ssn.SocialSecurityNumber;
import jakarta.inject.Named;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.Optional;

@Named
@RequiredArgsConstructor
@ConditionalOnProperty(
  value = "bank.loans.eligibility.credit-segment-adapter",
  havingValue = "sqlite"
)
class SQLiteCreditSegmentAdapter implements FindCreditSegment {

  private final CreditSegmentRepository repository;
  private final CreditSegmentMapper mapper;

  @Override
  public Optional<CreditSegment> forPerson(SocialSecurityNumber ssn) {
    return repository.findBySsn(ssn.value()).map(mapper::toDomain);
  }
}
