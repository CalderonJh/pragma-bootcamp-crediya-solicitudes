package com.co.crediya.requests.model.loanapplication;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class LoanApplication {
  private UUID id;
  private UUID applicantId;
  private BigDecimal amount;
  private Integer termInMonths;
  private LoanType loanType;
  private LoanStatus loanStatus;
}
