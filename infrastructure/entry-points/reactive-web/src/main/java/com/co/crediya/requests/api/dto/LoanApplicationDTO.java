package com.co.crediya.requests.api.dto;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationDTO {
  private BigDecimal amount;
  private Integer termInMonths;
  private UUID loanTypeId;
}
