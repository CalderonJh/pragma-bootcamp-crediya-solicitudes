package com.co.crediya.requests.api.config;

import com.co.crediya.requests.api.Handler;
import com.co.crediya.requests.api.RouterRest;
import com.co.crediya.requests.api.client.AuthServiceClient;
import com.co.crediya.requests.usecase.loan.ApplyForLoanUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest
@ContextConfiguration(classes = {RouterRest.class, Handler.class, TestSecurityConfig.class})
@Import({CorsConfig.class, SecurityHeadersConfig.class})
class ConfigTest {

  @Autowired private WebTestClient webTestClient;
  @MockitoBean private ApplyForLoanUseCase applyForLoanUseCase;
  @MockitoBean private AuthServiceClient authServiceClient;

  @Test
  void corsConfigurationShouldAllowOrigins() {
    webTestClient
        .get()
        .uri("/health")
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .valueEquals(
            "Content-Security-Policy",
            "default-src 'self'; frame-ancestors 'self'; form-action 'self'")
        .expectHeader()
        .valueEquals("Strict-Transport-Security", "max-age=31536000;")
        .expectHeader()
        .valueEquals("X-Content-Type-Options", "nosniff")
        .expectHeader()
        .valueEquals("Server", "")
        .expectHeader()
        .valueEquals("Cache-Control", "no-store")
        .expectHeader()
        .valueEquals("Pragma", "no-cache")
        .expectHeader()
        .valueEquals("Referrer-Policy", "strict-origin-when-cross-origin");
  }
}
