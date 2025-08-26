package com.co.crediya.requests.api;

import com.co.crediya.requests.api.dto.LoanApplicationDTO;
import com.co.crediya.requests.api.mapper.LoanApplicationMapper;
import com.co.crediya.requests.usecase.loanapplication.LoanApplicationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class Handler {
  private final LoanApplicationUseCase useCase;

  public Mono<ServerResponse> listenPOSTApplyForLoan(ServerRequest serverRequest) {
    return serverRequest
        .bodyToMono(LoanApplicationDTO.class)
        .map(LoanApplicationMapper::toModel)
        .flatMap(useCase::applyForLoan)
        .then(Mono.defer(() -> ServerResponse.ok().build()));
  }
}
