package com.co.crediya.requests.model.loanapplication;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {
  private UUID id;
	private String name;
	private String lastName;
  private String email;
  private BigDecimal baseSalary;
}
