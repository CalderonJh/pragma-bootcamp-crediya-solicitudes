package com.co.crediya.requests.r2dbc.entity.loan;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("estados")
public class LoanStatusEntity {
  @Id private UUID id;

  @Column("nombre")
  private String name;

  @Column("descripcion")
  private String description;
}
