package com.co.crediya.requests.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotifyStatusType {
  APPROVED("APROBADA", "LOAN_APPROVED"),
  REJECTED("RECHAZADA", "LOAN_REJECTED");

  private final String dbValue;
  private final String msgKey;

  public static NotifyStatusType fromDBValue(String value) {
    for (NotifyStatusType type : values())
      if (type.getDbValue().equalsIgnoreCase(value)) return type;
    throw new IllegalArgumentException("Unknown NotifyStatusType: " + value);
  }

  public static boolean mustNotify(String dbValue) {
    for (NotifyStatusType type : values())
      if (type.getDbValue().equalsIgnoreCase(dbValue)) return true;
    return false;
  }
}
