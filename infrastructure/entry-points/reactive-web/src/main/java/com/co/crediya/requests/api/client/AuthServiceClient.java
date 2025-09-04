package com.co.crediya.requests.api.client;

import com.co.crediya.requests.api.dto.LoanApplicantDTO;
import com.co.crediya.requests.api.dto.UserDTO;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class AuthServiceClient {
  private final WebClient webClient;
  private static final String AUTH_SERVICE_URL = "http://localhost:8081/api/v1";

  public AuthServiceClient(WebClient.Builder builder) {
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

  public Flux<LoanApplicantDTO> getApplicants(Set<UUID> ids, String jwt) {
    return webClient
        .post()
        .uri("/usuarios/by-ids")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
        .bodyValue(Map.of("ids", ids))
        .retrieve()
        .bodyToFlux(LoanApplicantDTO.class);
  }
}
