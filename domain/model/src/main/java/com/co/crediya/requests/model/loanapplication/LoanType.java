package com.co.crediya.requests.model.loanapplication;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanType {
  private UUID id;
	private String description;
  private BigDecimal minAmount;
  private BigDecimal maxAmount;
  private BigDecimal interestRate;
  private Boolean autoValidate;

  public LoanType(UUID id) {
    this.id = id;
  }
}
