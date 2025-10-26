package ee.rsx.kata.bank.loans.adapter.eligibility.db.entity;

import ee.rsx.kata.bank.loans.domain.segment.CreditSegmentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(name = "credit_segments")
@Getter
@Setter
@Accessors(fluent = false)
@NoArgsConstructor
@AllArgsConstructor
public class CreditSegmentEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String uuid;

  @Column(nullable = false, unique = true)
  private String ssn;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CreditSegmentType type;

  @Column(name = "credit_modifier", nullable = false)
  private Integer creditModifier;
}
