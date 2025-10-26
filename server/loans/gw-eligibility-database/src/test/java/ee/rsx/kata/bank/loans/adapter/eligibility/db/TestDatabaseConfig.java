package ee.rsx.kata.bank.loans.adapter.eligibility.db;

import ee.rsx.kata.bank.loans.adapter.eligibility.db.entity.CreditSegmentEntity;
import ee.rsx.kata.bank.loans.adapter.eligibility.db.mapper.CreditSegmentMapper;
import ee.rsx.kata.bank.loans.adapter.eligibility.db.repository.CreditSegmentRepository;
import ee.rsx.kata.bank.loans.domain.segment.gateway.FindCreditSegment;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.context.annotation.Bean;

@SpringBootConfiguration
@EnableAutoConfiguration
@EntityScan(basePackageClasses = CreditSegmentEntity.class)
@EnableJpaRepositories(basePackageClasses = CreditSegmentRepository.class)
class TestDatabaseConfig {

  @Bean
  CreditSegmentMapper creditSegmentMapper() {
    return new CreditSegmentMapper();
  }

  @Bean
  FindCreditSegment sqliteCreditSegmentAdapter(
    CreditSegmentRepository repository,
    CreditSegmentMapper mapper
  ) {
    return new SQLiteCreditSegmentAdapter(repository, mapper);
  }
}
