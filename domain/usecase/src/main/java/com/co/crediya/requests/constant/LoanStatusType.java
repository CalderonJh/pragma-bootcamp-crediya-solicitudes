package com.co.crediya.requests.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LoanStatusType {
  APPROVED("APROBADA"),
  REJECTED("RECHAZADA"),
  PENDING("PENDIENTE"),
  MANUAL_REVIEW("REVISION_MANUAL");

  private final String dbValue;

  public static LoanStatusType fromDBValue(String value) {
    for (LoanStatusType type : values()) if (type.getDbValue().equalsIgnoreCase(value)) return type;
    throw new IllegalArgumentException("Unknown LoanStatusType: " + value);
  }
}
