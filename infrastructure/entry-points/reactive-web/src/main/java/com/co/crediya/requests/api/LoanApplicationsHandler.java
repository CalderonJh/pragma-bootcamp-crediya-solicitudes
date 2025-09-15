package com.co.crediya.requests.api;

import static com.co.crediya.requests.api.util.Constant.LOAN_APL_ID_PARAM;
import static com.co.crediya.requests.api.util.Constant.LOAN_STATUS_ID_PARAM;
import static com.co.crediya.requests.api.util.WebTools.extractPageable;

import com.co.crediya.requests.api.client.AuthServiceClient;
import com.co.crediya.requests.api.dto.CreateLoanApplicationDTO;
import com.co.crediya.requests.api.dto.LoanApplicantDTO;
import com.co.crediya.requests.api.dto.LoanApplicationDTO;
import com.co.crediya.requests.api.dto.UserDTO;
import com.co.crediya.requests.api.mapper.LoanApplicationMapper;
import com.co.crediya.requests.api.util.ApiClient;
import com.co.crediya.requests.api.util.WebTools;
import com.co.crediya.requests.exception.DataNotFoundException;
import com.co.crediya.requests.model.util.Actor;
import com.co.crediya.requests.model.loanapplication.LoanApplication;
import com.co.crediya.requests.model.loanapplication.LoanApplicationFilter;
import com.co.crediya.requests.model.util.pagination.Page;
import com.co.crediya.requests.usecase.loan.ApplyForLoanUseCase;
import com.co.crediya.requests.usecase.loan.FindLoanApplicationsUseCase;
import com.co.crediya.requests.usecase.loan.UpdateAutoApprovedLoanUseCase;
import com.co.crediya.requests.usecase.loan.UpdateLoanApplicationUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
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
public class LoanApplicationsHandler {
  private final ApplyForLoanUseCase applyForLoanUseCase;
  private final FindLoanApplicationsUseCase findLoanApplicationsUseCase;
  private final UpdateLoanApplicationUseCase updateLoanApplicationUseCase;
  private final UpdateAutoApprovedLoanUseCase updateAutoApprovedLoanUseCase;
  private final AuthServiceClient authServiceClient;

  private static final Logger logger = Logger.getLogger(LoanApplicationsHandler.class.getName());

  @Operation(
      operationId = "applyForLoan",
      summary = "Registra una nueva solicitud de préstamo",
      requestBody =
          @RequestBody(
              content =
                  @Content(schema = @Schema(implementation = CreateLoanApplicationDTO.class))),
      responses = {@ApiResponse(responseCode = "201", content = @Content())})
  public Mono<ServerResponse> applyForLoan(ServerRequest serverRequest) {
    Mono<CreateLoanApplicationDTO> loanApplicationBody =
        serverRequest.bodyToMono(CreateLoanApplicationDTO.class);
    return serverRequest
        .principal()
        .cast(JwtAuthenticationToken.class)
        .flatMap(
            jwtAuthenticationToken -> {
              Jwt jwt = jwtAuthenticationToken.getToken();
              UUID actorId = UUID.fromString(jwt.getSubject());
              // call auth ms to get user details
              Mono<UserDTO> user =
                  authServiceClient
                      .getUser(actorId)
                      .switchIfEmpty(Mono.error(new DataNotFoundException("User not found")))
                      .doOnNext(
                          u -> logger.info("Fetched user details: %s".formatted(u.toString())));
              return user.zipWith(loanApplicationBody)
                  .flatMap(
                      tuple -> {
                        LoanApplication loanApplication =
                            LoanApplicationMapper.toModel(tuple.getT2(), actorId);
                        UserDTO applicant = tuple.getT1();
                        return applyForLoanUseCase.execute(
                            loanApplication, new Actor(applicant.id(), applicant.role()));
                      });
            })
        .then(ServerResponse.status(HttpStatus.CREATED).build());
  }

  @Operation(
      operationId = "getLoanApplicationsPage",
      summary = "Consulta solicitudes de préstamo",
      responses = {
        @ApiResponse(
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = LoanApplicationDTO.class)))
      })
  @Parameter(name = "page", description = "Número de página (0-indexado)", example = "0")
  @Parameter(name = "size", description = "Tamaño de página", example = "20")
  @Parameter(name = "sort", description = "Criterios de ordenamiento", example = "amount,asc")
  @Parameter(name = "statusId", description = "Filtra por id de estado de la solicitud")
  public Mono<ServerResponse> getLoanApplicationsPage(ServerRequest serverRequest) {
    return WebTools.extractApiClient(serverRequest)
        .flatMap(
            apiClient -> {
              UUID statusId =
                  serverRequest.queryParam(LOAN_STATUS_ID_PARAM).map(UUID::fromString).orElse(null);
              return findLoanApplicationsUseCase
                  .execute(
                      extractPageable(serverRequest),
                      new LoanApplicationFilter(statusId),
                      apiClient.actor())
                  .flatMap(page -> mapToLoanApplicationsWithApplicantInfo(page, apiClient))
                  .flatMap(
                      page ->
                          ServerResponse.ok()
                              .contentType(MediaType.APPLICATION_JSON)
                              .bodyValue(page));
            });
  }

  private Mono<Page<LoanApplicationDTO>> mapToLoanApplicationsWithApplicantInfo(
      Page<LoanApplication> loanApplicationPage, ApiClient apiClient) {

    Set<UUID> applicantIdSet =
        loanApplicationPage.getContent().stream()
            .map(LoanApplication::getApplicantId)
            .collect(Collectors.toSet());

    return authServiceClient
        .getApplicants(applicantIdSet, apiClient.jwt())
        .collectMap(LoanApplicantDTO::id, dto -> dto)
        .map(
            applicantMap -> {
              List<LoanApplicationDTO> pageContent =
                  loanApplicationPage.getContent().stream()
                      .map(
                          app ->
                              LoanApplicationMapper.toResponse(
                                  app, applicantMap.get(app.getApplicantId())))
                      .toList();

              return new Page<>(
                  pageContent,
                  loanApplicationPage.getPageable(),
                  loanApplicationPage.getTotalElements());
            });
  }

  @Operation(
      operationId = "updateLoanAplStatus",
      summary = "Actualizar el estado de una solicitud de crédito",
      responses = {
        @ApiResponse(
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = LoanApplication.class)))
      })
  @Parameter(name = "applicationId", description = "Id de la solicitud", example = "uuid")
  @Parameter(name = "statusId", description = "Estado a asignar", example = "uuid")
  public Mono<ServerResponse> updateLoanAplStatus(ServerRequest serverRequest) {
    return WebTools.extractActor(serverRequest)
        .flatMap(
            actor -> {
              Optional<String> applicationId = serverRequest.queryParam(LOAN_APL_ID_PARAM);
              Optional<String> statusId = serverRequest.queryParam(LOAN_STATUS_ID_PARAM);
              return updateLoanApplicationUseCase
                  .execute(
                      applicationId.map(UUID::fromString).orElse(null),
                      statusId.map(UUID::fromString).orElse(null),
                      actor)
                  .flatMap(res -> ServerResponse.status(HttpStatus.OK).bodyValue(res));
            });
  }

  public Mono<ServerResponse> updateAutoApprovedApl(ServerRequest serverRequest) {
    return WebTools.extractActor(serverRequest)
        .flatMap(
            actor -> {
              Optional<String> applicationId = serverRequest.queryParam(LOAN_APL_ID_PARAM);
              Optional<String> result = serverRequest.queryParam("result");
              return updateAutoApprovedLoanUseCase
                  .execute(applicationId.map(UUID::fromString).orElse(null), result.orElse(null))
                  .flatMap(res -> ServerResponse.status(HttpStatus.OK).bodyValue(res));
            });
  }
}
