package com.co.crediya.requests.api.util;

import static com.co.crediya.requests.api.util.Constant.ROLE_CLAIM_KEY;

import com.co.crediya.requests.exception.PermissionException;
import com.co.crediya.requests.model.loanapplication.Actor;
import com.co.crediya.requests.model.util.pagination.Pageable;
import com.co.crediya.requests.model.util.pagination.Sort;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

public final class WebTools {

  private WebTools() {}

  /** Extract email (subject) and role (claim) from the token */
  public static Mono<Actor> extractActor(JwtAuthenticationToken token) {
    String id = token.getToken().getSubject();

    Object roleClaim = token.getToken().getClaims().get(ROLE_CLAIM_KEY);

    if (id == null) return Mono.error(new PermissionException("Missing sub in token"));

    if (roleClaim == null || roleClaim.toString().isBlank())
      return Mono.error(new PermissionException("Missing role in token"));

    return Mono.just(new Actor(UUID.fromString(id), roleClaim.toString()));
  }

  public static Mono<String> extractToken(ServerRequest serverRequest) {
    return Mono.justOrEmpty(serverRequest.headers().firstHeader(HttpHeaders.AUTHORIZATION))
        .filter(header -> header.startsWith("Bearer "))
        .map(header -> header.substring(7))
        .switchIfEmpty(
            Mono.error(new PermissionException("Missing or invalid Authorization header")));
  }

  /** Extract Actor from ServerRequest */
  public static Mono<Actor> extractActor(ServerRequest serverRequest) {
    return serverRequest
        .principal()
        .cast(JwtAuthenticationToken.class)
        .switchIfEmpty(Mono.error(new PermissionException("Missing authentication principal")))
        .flatMap(WebTools::extractActor);
  }

  public static Mono<ApiClient> extractApiClient(ServerRequest serverRequest) {
    return extractToken(serverRequest)
        .zipWith(extractActor(serverRequest))
        .map(tuple -> new ApiClient(tuple.getT2(), tuple.getT1()));
  }

  public static Pageable extractPageable(ServerRequest request) {
    int page = request.queryParam("page").map(Integer::parseInt).orElse(0);
    int size = request.queryParam("size").map(Integer::parseInt).orElse(20);

    // Collect sort params
    List<Sort.Order> orders =
        request.queryParams().getOrDefault("sort", List.of()).stream()
            .map(
                param -> {
                  String[] parts = param.split(",");
                  String property = parts[0].trim();
                  Sort.Direction direction =
                      (parts.length > 1) ? Sort.Direction.get(parts[1].trim()) : Sort.Direction.ASC;
                  return new Sort.Order(direction, property);
                })
            .toList();

    Sort sort = new Sort(orders);
    return new Pageable(page, size, sort);
  }
}
