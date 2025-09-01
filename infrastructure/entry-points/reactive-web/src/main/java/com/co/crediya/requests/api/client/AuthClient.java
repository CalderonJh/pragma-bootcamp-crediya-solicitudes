package com.co.crediya.requests.api.client;

import com.co.crediya.requests.api.dto.UserDTO;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class AuthClient {
  private final WebClient webClient;
  private static final String AUTH_SERVICE_URL = "http://localhost:8081/api/v1";

  public AuthClient(WebClient.Builder builder) {
    this.webClient = builder.baseUrl(AUTH_SERVICE_URL).build();
  }

  public Mono<UserDTO> getUser(UUID id, String jwt) {
    return webClient
        .get()
        .uri("/usuarios/{id}", id)
        .header("Authorization", "Bearer " + jwt)
        .retrieve()
        .bodyToMono(UserDTO.class);
  }
}
