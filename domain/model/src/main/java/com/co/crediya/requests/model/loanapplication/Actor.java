package com.co.crediya.requests.model.loanapplication;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
public class Actor {
  private String email;
  private String role;
}
