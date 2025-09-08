package com.co.crediya.requests.api;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

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
        beanClass = LoanApplicationsHandler.class,
        beanMethod = "applyForLoan"),
    @RouterOperation(
        path = "/api/v1/solicitudes",
        method = RequestMethod.GET,
        beanClass = LoanApplicationsHandler.class,
        beanMethod = "getLoanApplicationsPage"),
    @RouterOperation(
        path = "/api/v1/solicitudes/estado",
        method = RequestMethod.PATCH,
        beanClass = LoanApplicationsHandler.class,
        beanMethod = "updateLoanAplStatus")
  })
  @Bean
  public RouterFunction<ServerResponse> routerFunction(LoanApplicationsHandler handler) {
    return route(POST("/api/v1/solicitudes"), handler::applyForLoan)
        .andRoute(PATCH("/api/v1/solicitudes/estado"), handler::updateLoanAplStatus)
        .and(route(GET("/api/v1/solicitudes"), handler::getLoanApplicationsPage));
  }

  @Bean
  public RouterFunction<ServerResponse> health() {
    return route(GET("/health"), request -> ServerResponse.ok().bodyValue("OK"));
  }
}
