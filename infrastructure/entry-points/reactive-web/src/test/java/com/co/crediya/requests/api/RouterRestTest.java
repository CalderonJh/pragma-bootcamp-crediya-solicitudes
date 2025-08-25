package com.co.crediya.requests.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.co.crediya.requests.api.config.LoanApplicationPath;
import com.co.crediya.requests.api.dto.LoanApplicationDTO;
import com.co.crediya.requests.usecase.loanapplication.LoanApplicationUseCase;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest
@ContextConfiguration(classes = {RouterRest.class, Handler.class})
@EnableConfigurationProperties(LoanApplicationPath.class)
class RouterRestTest {

  @Autowired private WebTestClient webTestClient;
  @MockitoBean private LoanApplicationUseCase loanApplicationUseCase;

  @Test
  void testListenPOSTApplyForLoan() {
    LoanApplicationDTO dto =
        LoanApplicationDTO.builder()
            .applicantEmail("email@email.com")
            .amount(BigDecimal.valueOf(10000000))
            .termInMonths(12)
            .loanTypeId(UUID.randomUUID())
            .build();
    when(loanApplicationUseCase.applyForLoan(any())).thenReturn(Mono.empty());
    webTestClient
        .post()
        .uri("/api/v1/solicitud")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(dto)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .isEmpty();
  }
}
