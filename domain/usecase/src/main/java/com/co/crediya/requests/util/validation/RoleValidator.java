package com.co.crediya.requests.util.validation;

import com.co.crediya.requests.constant.RoleType;
import com.co.crediya.requests.exception.PermissionException;
import com.co.crediya.requests.model.loanapplication.Actor;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import reactor.core.publisher.Mono;

public class RoleValidator {
  private RoleValidator() {}

  private static final Logger logger = Logger.getLogger(RoleValidator.class.getName());

  public static final String PERMISSION_DENIED_MESSAGE = "No permission to perform this action";

  public static Mono<Void> hasRole(Actor actor, RoleType... options) {
    for (RoleType type : options)
      if (type.getValue().equalsIgnoreCase(actor.getRole())) return Mono.empty();

    logger.log(
        Level.WARNING,
        () ->
            "Permission denied for actor %s, required role: %s "
                .formatted(actor, Stream.of(options).map(RoleType::getValue).toList()));
    return Mono.error(new PermissionException(PERMISSION_DENIED_MESSAGE));
  }
}
