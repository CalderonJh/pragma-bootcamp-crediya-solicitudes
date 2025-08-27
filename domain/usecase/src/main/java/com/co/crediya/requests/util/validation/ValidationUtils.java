package com.co.crediya.requests.util.validation;

import java.math.BigDecimal;

public class ValidationUtils {
  private ValidationUtils() {}

  public static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

  public static boolean isNull(Object obj) {
    return obj == null;
  }

  public static boolean isValidEmail(String email) {
    return hasText(email) && email.matches(EMAIL_REGEX);
  }

  public static boolean hasText(String s) {
    return s != null && !s.isBlank();
  }

  public static boolean inRange(BigDecimal number, BigDecimal min, BigDecimal max) {
    if (number == null || min == null || max == null) {
      return false;
    }
    return number.compareTo(min) >= 0 && number.compareTo(max) <= 0;
  }

  public static boolean isMin(BigDecimal number, BigDecimal min) {
    if (number == null || min == null) {
      return false;
    }
    return number.compareTo(min) >= 0;
  }

  public static boolean isMax(BigDecimal number, BigDecimal max) {
    if (number == null || max == null) {
      return false;
    }
    return number.compareTo(max) <= 0;
  }
}
