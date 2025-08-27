package com.co.crediya.requests.util.validation;

import static com.co.crediya.requests.util.validation.ValidationUtils.*;

import com.co.crediya.requests.exception.BusinessRuleException;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

public class ReactiveValidators {
  private ReactiveValidators() {}

  @AllArgsConstructor
  public enum MessageTemplate {
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

  public static Mono<Void> email(String email) {
    return isValidEmail(email)
        ? Mono.empty()
        : Mono.error(new BusinessRuleException(MessageTemplate.EMAIL.render()));
  }

  public static Mono<Void> notNull(Object value, String field) {
    return value != null
        ? Mono.empty()
        : Mono.error(new BusinessRuleException(MessageTemplate.NOT_NULL.render(field)));
  }

  public static Mono<Void> hasText(String value, String field) {
    return ValidationUtils.hasText(value)
        ? Mono.empty()
        : Mono.error(new BusinessRuleException(MessageTemplate.NOT_EMPTY.render(field)));
  }

  @SuppressWarnings("unchecked")
  public static <T> Mono<Void> range(
      Comparable<T> comparable, Comparable<T> minimum, Comparable<T> maximum, String field) {
    return (comparable.compareTo((T) minimum) >= 0 && comparable.compareTo((T) maximum) <= 0)
        ? Mono.empty()
        : Mono.error(
            new BusinessRuleException(MessageTemplate.RANGE.render(field, minimum, maximum)));
  }

  public static Mono<Void> positive(Number number, String field) {
    return number != null && number.doubleValue() > 0
        ? Mono.empty()
        : Mono.error(new BusinessRuleException(MessageTemplate.POSITIVE.render(field)));
  }
}
