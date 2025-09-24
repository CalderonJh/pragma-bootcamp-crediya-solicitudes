package com.co.crediya.requests.consumer;

import com.co.crediya.requests.model.loanapplication.User;
import com.co.crediya.requests.model.loanapplication.gateways.UserService;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class UserRestConsumer implements UserService {
  private final WebClient client;

  public UserRestConsumer(
      WebClient.Builder builder,
      @Value("${services.auth.url}") String authUrl,
      @Value("${services.auth.api-key}") String apiKey) {
    this.client =
        builder
            .baseUrl(authUrl)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
            .build();
  }

  @Override
  public Mono<User> getUserById(UUID applicantId) {
    return client.get().uri("/usuarios/{id}", applicantId).retrieve().bodyToMono(User.class);
  }

  @Override
  public Flux<User> getByRole(String role) {
    return client
        .get()
        .uri(uriBuilder -> uriBuilder.path("/usuarios/buscar/rol").queryParam("rol", role).build())
        .retrieve()
        .bodyToFlux(User.class);
  }
}
