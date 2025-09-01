package com.co.crediya.requests.api.exception.handler;

import com.co.crediya.requests.exception.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

  private final ObjectMapper objectMapper;
  private static final Map<Class<? extends Throwable>, HttpStatus> EXCEPTION_STATUS_MAP =
      Map.of(
          ConflictException.class,
          HttpStatus.CONFLICT,
          ValidationException.class,
          HttpStatus.BAD_REQUEST,
          PermissionException.class,
          HttpStatus.FORBIDDEN,
          InternalException.class,
          HttpStatus.INTERNAL_SERVER_ERROR,
          DataNotFoundException.class,
          HttpStatus.NOT_FOUND);

  public static final Set<HttpStatus> NON_REPORTABLE =
      Set.of(
          HttpStatus.BAD_REQUEST,
          HttpStatus.NOT_FOUND,
          HttpStatus.UNAUTHORIZED,
          HttpStatus.FORBIDDEN);

  @Override
  public @NonNull Mono<Void> handle(@NonNull ServerWebExchange exchange, @NonNull Throwable ex) {
    String requestPath = exchange.getRequest().getPath().value();
    String method = exchange.getRequest().getMethod().name();

    HttpStatus status = determineHttpStatus(ex);
    if (NON_REPORTABLE.contains(status))
      log.warn("Problem in {} {}: {}", method, requestPath, ex.getMessage());
    else log.error("Error in {} {}: ", method, requestPath, ex);

    ErrorResponse errorResponse = buildErrorResponse(ex, requestPath);

    exchange.getResponse().setStatusCode(status);
    exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

    return writeErrorResponse(exchange, errorResponse);
  }

  private ErrorResponse buildErrorResponse(Throwable ex, String path) {
    return ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .path(path)
        .error(ex.getClass().getSimpleName())
        .message(extractMessage(ex))
        .status(determineHttpStatus(ex).value())
        .build();
  }

  private HttpStatus determineHttpStatus(Throwable ex) {
    return EXCEPTION_STATUS_MAP.getOrDefault(ex.getClass(), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private String extractMessage(Throwable ex) {
    return switch (ex) {
      case ConflictException businessEx -> businessEx.getMessage();
      case DataNotFoundException dataEx -> dataEx.getMessage();
      case InternalException ignored -> "An internal error occurred";
      case ResponseStatusException responseEx -> responseEx.getReason();
      case IllegalArgumentException argEx -> argEx.getMessage();
      default -> "An unexpected error occurred";
    };
  }

  private Mono<Void> writeErrorResponse(ServerWebExchange exchange, ErrorResponse errorResponse) {
    try {
      byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
      DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
      return exchange.getResponse().writeWith(Mono.just(buffer));
    } catch (JsonProcessingException e) {
      log.error("Error serializing error response", e);
      String fallback = "{\"error\":\"Internal server error\"}";
      DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(fallback.getBytes());
      return exchange.getResponse().writeWith(Mono.just(buffer));
    }
  }

  @Builder
  public record ErrorResponse(
      LocalDateTime timestamp, int status, String error, String message, String path) {}
}
