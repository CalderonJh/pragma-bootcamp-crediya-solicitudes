package com.co.crediya.requests.r2dbc.entity.loan;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table("solicitudes")
public class LoanApplicationEntity {
  @Id
  @Column("id_solicitud")
  private UUID id;

  @Column("applicant_id")
  private UUID applicantId;

  @Column("monto")
  private BigDecimal amount;

  @Column("plazo")
  private Integer termInMonths;

  @Column("id_tipo_prestamo")
  private UUID loanTypeId;

  @Column("id_estado")
  private UUID loanStatusId;
}
