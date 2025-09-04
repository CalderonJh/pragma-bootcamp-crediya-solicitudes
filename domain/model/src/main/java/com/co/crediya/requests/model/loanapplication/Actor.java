package com.co.crediya.requests.model.loanapplication;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class Actor {
  private UUID id;
  private String role;
}
