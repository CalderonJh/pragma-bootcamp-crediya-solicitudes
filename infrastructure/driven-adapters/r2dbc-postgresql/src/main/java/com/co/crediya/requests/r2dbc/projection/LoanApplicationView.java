package com.co.crediya.requests.r2dbc.projection;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Column;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoanApplicationView {
  @Column("id")
  private UUID id;

  @Column("applicant_id")
  private UUID applicantId;

  @Column("amount")
  private BigDecimal amount;

  @Column("term_in_months")
  private Integer termInMonths;

  @Column("loan_type_id")
  private UUID loanTypeId;

  @Column("loan_type")
  private String loanTypeDescription;

  @Column("loan_type_min_amount")
  private BigDecimal loanTypeMinAmount;

  @Column("loan_type_max_amount")
  private BigDecimal loanTypeMaxAmount;

  @Column("loan_type_interest_rate")
  private BigDecimal loanTypeInterestRate;

  @Column("loan_type_auto_validate")
  private Boolean loanTypeAutoValidate;

  @Column("loan_status_id")
  private UUID loanStatusId;

  @Column("loan_status")
  private String loanStatusName;

  @Column("loan_status_description")
  private String loanStatusDescription;
}
