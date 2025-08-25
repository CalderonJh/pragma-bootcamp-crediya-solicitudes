package com.co.crediya.requests.api.exception.handler;

import com.co.crediya.requests.usecase.exception.BusinessRuleException;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

  @Override
  public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
    log.error("Exception caught: ", ex);
    HttpStatus status;
    String message;

    if (ex instanceof BusinessRuleException) {
      status = HttpStatus.BAD_REQUEST;
      message = ex.getMessage();
    } else {
      status = HttpStatus.INTERNAL_SERVER_ERROR;
      message = "Unexpected error occurred";
    }

    exchange.getResponse().setStatusCode(status);
    exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

    DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
    DataBuffer buffer =
        bufferFactory.wrap(("{\"error\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8));

    return exchange.getResponse().writeWith(Mono.just(buffer));
  }
}
