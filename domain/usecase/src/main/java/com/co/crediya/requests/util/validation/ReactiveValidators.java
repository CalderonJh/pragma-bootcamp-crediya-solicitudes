package com.co.crediya.requests.util.validation;

import static com.co.crediya.requests.util.validation.ValidationUtils.*;

import com.co.crediya.requests.exception.BusinessRuleException;
import reactor.core.publisher.Mono;

public class ReactiveValidators {
  private ReactiveValidators() {}

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
