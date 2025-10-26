package ee.rsx.kata.bank.loans.adapter.eligibility.db.repository;

import ee.rsx.kata.bank.loans.adapter.eligibility.db.entity.CreditSegmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CreditSegmentRepository extends JpaRepository<CreditSegmentEntity, Long> {

  Optional<CreditSegmentEntity> findBySsn(String ssn);
}
