package com.co.crediya.requests.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

import com.co.crediya.requests.api.client.AuthServiceClient;
import com.co.crediya.requests.api.config.TestSecurityConfig;
import com.co.crediya.requests.api.dto.CreateLoanApplicationDTO;
import com.co.crediya.requests.api.dto.UserDTO;
import com.co.crediya.requests.usecase.loan.ApplyForLoanUseCase;
import com.co.crediya.requests.usecase.loan.FindLoanApplicationsUseCase;
import java.math.BigDecimal;
import java.util.UUID;

import com.co.crediya.requests.usecase.loan.UpdateAutoApprovedLoanUseCase;
import com.co.crediya.requests.usecase.loan.UpdateLoanApplicationUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest
@ContextConfiguration(classes = {RouterRest.class, LoanApplicationsHandler.class, TestSecurityConfig.class})
class RouterRestTest {

  @Autowired private WebTestClient webTestClient;
  @MockitoBean private ApplyForLoanUseCase applyForLoanUseCase;
  @MockitoBean private FindLoanApplicationsUseCase findLoanApplicationsUseCase;
  @MockitoBean private AuthServiceClient authServiceClient;
  @MockitoBean private UpdateLoanApplicationUseCase updateLoanApplicationUseCase;
	@MockitoBean private UpdateAutoApprovedLoanUseCase updateAutoApprovedLoanUseCase;

  @Test
  @DisplayName("POST - Apply for Loan")
  void testListenPOSTApplyForLoan() {
    CreateLoanApplicationDTO dto =
        new CreateLoanApplicationDTO(BigDecimal.valueOf(100), 1, UUID.randomUUID());
    when(authServiceClient.getUser(any()))
        .thenReturn(Mono.just(new UserDTO(UUID.randomUUID(), "email@email.com", "USER")));
    when(applyForLoanUseCase.execute(any(), any())).thenReturn(Mono.empty());
    webTestClient
        .mutateWith(
            mockJwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString()).claim("role", "USER")))
        .post()
        .uri("/api/v1/solicitudes")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(dto)
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody()
        .isEmpty();
  }
}
