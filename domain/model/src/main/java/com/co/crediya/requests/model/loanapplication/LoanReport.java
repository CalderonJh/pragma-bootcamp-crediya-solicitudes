package com.co.crediya.requests.model.loanapplication;

import java.math.BigDecimal;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoanReport {
  private Long totalLoans;
  private BigDecimal totalAmount;
}
