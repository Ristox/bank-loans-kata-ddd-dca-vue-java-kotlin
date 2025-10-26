package ee.rsx.kata.bank.loans.adapter.eligibility.db;

import ee.rsx.kata.bank.loans.domain.segment.CreditSegmentType;
import ee.rsx.kata.bank.loans.domain.segment.gateway.FindCreditSegment;
import ee.rsx.kata.bank.loans.domain.ssn.SocialSecurityNumber;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static ee.rsx.kata.bank.loans.domain.segment.CreditSegmentType.*;
import static org.assertj.core.api.Assertions.*;
import java.util.stream.Stream;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestDatabaseConfig.class)
@ActiveProfiles("database-test-sqlite")
@TestPropertySource(properties = "bank.loans.eligibility.credit-segment-adapter=sqlite")
@DisplayName("Credit segment SQLite database")
class CreditSegmentSqliteDatabaseTest {

  @Autowired
  private FindCreditSegment findCreditSegment;

  @ParameterizedTest(name = "finds {1} segment for SSN {0}")
  @MethodSource("seededSegments")
  void returnsSegmentForSeededEntries(String ssn, CreditSegmentType expectedType, int expectedModifier) {
    var creditSegment = findCreditSegment.forPerson(new SocialSecurityNumber(ssn));

    assertThat(creditSegment)
      .hasValueSatisfying(segment -> {
        assertThat(segment.type()).isEqualTo(expectedType);
        assertThat(segment.creditModifier()).isEqualTo(expectedModifier);
      });
  }

  private static Stream<Arguments> seededSegments() {
    return Stream.of(
      Arguments.of("49002010965", DEBT, 0),
      Arguments.of("49002010976", SEGMENT_1, 100),
      Arguments.of("49002010987", SEGMENT_2, 300),
      Arguments.of("49002010998", SEGMENT_3, 1000)
    );
  }
}
