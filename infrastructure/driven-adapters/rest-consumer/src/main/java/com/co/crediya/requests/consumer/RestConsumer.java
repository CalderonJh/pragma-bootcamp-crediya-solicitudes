package com.co.crediya.requests.consumer;

import com.co.crediya.requests.model.loanapplication.Applicant;
import com.co.crediya.requests.model.loanapplication.gateways.ApplicantService;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class RestConsumer implements ApplicantService /* implements Gateway from domain */ {
  private final WebClient client;

  public RestConsumer(
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
  public Mono<Applicant> getApplicantById(UUID applicantId) {
    return client.get().uri("/usuarios/{id}", applicantId).retrieve().bodyToMono(Applicant.class);
  }
}
