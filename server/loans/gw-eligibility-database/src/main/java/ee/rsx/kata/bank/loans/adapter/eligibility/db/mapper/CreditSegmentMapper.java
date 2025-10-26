package ee.rsx.kata.bank.loans.adapter.eligibility.db.mapper;

import ee.rsx.kata.bank.loans.adapter.eligibility.db.entity.CreditSegmentEntity;
import ee.rsx.kata.bank.loans.domain.segment.CreditSegment;
import ee.rsx.kata.bank.loans.domain.ssn.SocialSecurityNumber;
import jakarta.inject.Named;

@Named
public class CreditSegmentMapper {

  public CreditSegment toDomain(CreditSegmentEntity entity) {
    if (entity == null) {
      return null;
    }
    if (entity.getType() == null) {
      throw new IllegalStateException("Credit segment type must not be null for SSN " + entity.getSsn());
    }
    if (entity.getCreditModifier() == null || entity.getCreditModifier() <= 0) {
      throw new IllegalStateException(
        "Credit modifier must be positive for SSN " + entity.getSsn()
      );
    }
    return new CreditSegment(
      new SocialSecurityNumber(entity.getSsn()),
      entity.getType(),
      entity.getCreditModifier()
    );
  }
}
