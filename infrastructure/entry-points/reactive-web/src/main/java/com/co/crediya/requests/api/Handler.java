package com.co.crediya.requests.api;

import com.co.crediya.requests.api.client.AuthClient;
import com.co.crediya.requests.api.dto.LoanApplicationDTO;
import com.co.crediya.requests.api.dto.UserDTO;
import com.co.crediya.requests.api.mapper.LoanApplicationMapper;
import com.co.crediya.requests.exception.DataNotFoundException;
import com.co.crediya.requests.model.loanapplication.Actor;
import com.co.crediya.requests.model.loanapplication.LoanApplication;
import com.co.crediya.requests.usecase.loan.LoanApplicationUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Tag(name = "Solicitudes de crédito")
public class Handler {
  private final LoanApplicationUseCase useCase;
  private final AuthClient authClient;
  private static final Logger logger = Logger.getLogger(Handler.class.getName());

  @Operation(
      operationId = "applyForLoan",
      summary = "Registra una nueva solicitud de préstamo",
      requestBody =
          @RequestBody(
              content = @Content(schema = @Schema(implementation = LoanApplicationDTO.class))),
      responses = {@ApiResponse(responseCode = "201", content = @Content())})
  public Mono<ServerResponse> listenPOSTApplyForLoan(ServerRequest serverRequest) {
    Mono<LoanApplicationDTO> loanApplicationBody =
        serverRequest.bodyToMono(LoanApplicationDTO.class);
    return serverRequest
        .principal()
        .cast(JwtAuthenticationToken.class)
        .flatMap(
            jwtAuthenticationToken -> {
              Jwt jwt = jwtAuthenticationToken.getToken();
              UUID actorId = UUID.fromString(jwt.getSubject());
              // call auth ms to get user details
              Mono<UserDTO> user =
                  authClient
                      .getUser(actorId, jwt.getTokenValue())
                      .switchIfEmpty(Mono.error(new DataNotFoundException("User not found")))
                      .doOnNext(
                          u -> logger.info("Fetched user details: %s".formatted(u.toString())));
              return user.zipWith(loanApplicationBody)
                  .flatMap(
                      tuple -> {
                        LoanApplication loanApplication =
                            LoanApplicationMapper.toModel(tuple.getT2(), tuple.getT1().getEmail());
                        return useCase.saveLoanApplication(
                            loanApplication,
                            new Actor(tuple.getT1().getEmail(), tuple.getT1().getRole()));
                      });
            })
        .then(ServerResponse.status(HttpStatus.CREATED).build());
  }

  @Operation(
      operationId = "getAllLoanApplications",
      summary = "Consulta todas las solicitudes de préstamo",
      requestBody =
          @RequestBody(
              content = @Content(schema = @Schema(implementation = LoanApplicationDTO.class))),
      responses = {
        @ApiResponse(
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = LoanApplication.class)))
      })
  public Mono<ServerResponse> listenGETAllLoanApplications() {
    return ServerResponse.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(useCase.getAllLoanApplications(), LoanApplication.class);
  }
}
