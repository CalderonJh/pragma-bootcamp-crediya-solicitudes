package com.co.crediya.requests.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

import com.co.crediya.requests.api.client.AuthClient;
import com.co.crediya.requests.api.config.TestSecurityConfig;
import com.co.crediya.requests.api.dto.LoanApplicationDTO;
import com.co.crediya.requests.api.dto.UserDTO;
import com.co.crediya.requests.usecase.loan.LoanApplicationUseCase;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest
@ContextConfiguration(classes = {RouterRest.class, Handler.class, TestSecurityConfig.class})
class RouterRestTest {

  @Autowired private WebTestClient webTestClient;
  @MockitoBean private LoanApplicationUseCase loanApplicationUseCase;
  @MockitoBean private AuthClient authClient;

  @Test
  @DisplayName("POST - Apply for Loan")
  void testListenPOSTApplyForLoan() {
    LoanApplicationDTO dto =
        LoanApplicationDTO.builder()
            .amount(BigDecimal.valueOf(10000000))
            .termInMonths(12)
            .loanTypeId(UUID.randomUUID())
            .build();
    when(authClient.getUser(any(), any())).thenReturn(Mono.just(new UserDTO()));
    when(loanApplicationUseCase.saveLoanApplication(any(), any())).thenReturn(Mono.empty());
    webTestClient
        .mutateWith(
            mockJwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString()).claim("role", "USER")))
        .post()
        .uri("/api/v1/solicitud")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(dto)
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody()
        .isEmpty();
  }

  @Test
  @DisplayName("GET - All Loan Applications")
  void testListenGETAllApplications() {
    when(loanApplicationUseCase.getAllLoanApplications()).thenReturn(Flux.empty());
    webTestClient
        .get()
        .uri("/api/v1/solicitudes")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .json("[]");
  }
}
