package com.co.crediya.requests.r2dbc.entity.loan;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("tipo_prestamo")
public class LoanTypeEntity {
  @Id private UUID id;

  @Column("monto_minimo")
  private BigDecimal minAmount;

  @Column("monto_maximo")
  private BigDecimal maxAmount;

  @Column("tasa_interes")
  private Double interestRate;

  @Column("validacion_automatica")
  private Boolean autoValidate;

  public LoanTypeEntity(UUID id) {
    this.id = id;
  }
}
