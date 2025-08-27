package com.co.crediya.requests.api;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import com.co.crediya.requests.api.dto.LoanApplicationDTO;
import com.co.crediya.requests.model.loanapplication.LoanApplication;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@RequiredArgsConstructor
public class RouterRest {

  @RouterOperations({
    @RouterOperation(
        path = "/api/v1/solicitud",
        method = RequestMethod.POST,
        beanClass = Handler.class,
        beanMethod = "listenPOSTApplyForLoan",
        operation =
            @Operation(
                operationId = "applyForLoan",
                summary = "Registra una nueva solicitud de préstamo",
                requestBody =
                    @RequestBody(
                        content =
                            @Content(schema = @Schema(implementation = LoanApplicationDTO.class))),
                responses = {@ApiResponse(responseCode = "200", content = @Content())})),
    @RouterOperation(
        path = "/api/v1/solicitudes",
        method = RequestMethod.GET,
        beanClass = Handler.class,
        beanMethod = "listenGETAllLoanApplications",
        operation =
            @Operation(
                operationId = "getAllLoanApplications",
                summary = "Consulta todas las solicitudes de préstamo",
                requestBody =
                    @RequestBody(
                        content =
                            @Content(schema = @Schema(implementation = LoanApplicationDTO.class))),
                responses = {
                  @ApiResponse(
                      responseCode = "200",
                      content = @Content(schema = @Schema(implementation = LoanApplication.class)))
                }))
  })
  @Bean
  public RouterFunction<ServerResponse> routerFunction(Handler handler) {
    return route(POST("/api/v1/solicitud"), handler::listenPOSTApplyForLoan)
        .and(route(GET("/api/v1/solicitudes"), s -> handler.listenGETAllLoanApplications()));
  }

  @Bean
  public RouterFunction<ServerResponse> health() {
    return route(GET("/health"), request -> ServerResponse.ok().bodyValue("OK"));
  }
}
