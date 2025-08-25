package com.co.crediya.requests.model.loanapplication;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoanStatus {
  private UUID id;
  private String name;
  private String description;
}
