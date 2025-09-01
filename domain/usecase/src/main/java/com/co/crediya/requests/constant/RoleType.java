package com.co.crediya.requests.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum RoleType {
  ADMIN("ADMIN"),
  USER("USUARIO"),
  CONSULTANT("ASESOR");

  private final String value;
}
