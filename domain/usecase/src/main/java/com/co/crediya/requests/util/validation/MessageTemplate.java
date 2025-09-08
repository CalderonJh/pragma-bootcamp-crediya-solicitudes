package com.co.crediya.requests.util.validation;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum MessageTemplate {
  NOT_AUTHORIZED("User not authorized to perform this action"),
  NOT_FOUND("%s not found"),
  EMAIL("The email format is invalid"),
  NOT_NULL("%s must not be null"),
  NOT_EMPTY("%s must not be empty"),
  POSITIVE("%s must be positive"),
  RANGE("%s must be between %s and %s");

	private final String template;
  public String render(Object... params) {
    if (params == null || params.length == 0) return this.template;
    return String.format(this.template, params);
  }
}
