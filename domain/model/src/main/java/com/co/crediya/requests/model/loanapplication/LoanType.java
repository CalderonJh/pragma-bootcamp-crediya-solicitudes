package com.co.crediya.requests.model.loanapplication;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanType {
  private UUID id;
  private BigDecimal minAmount;
  private BigDecimal maxAmount;
  private Double interestRate;
  private Boolean autoValidate;

  public LoanType(UUID id) {
    this.id = id;
  }
}
