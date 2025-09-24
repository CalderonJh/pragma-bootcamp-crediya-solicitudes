package com.co.crediya.requests.consumer;

import com.co.crediya.requests.model.loanapplication.LoanReport;
import com.co.crediya.requests.model.loanapplication.gateways.LoansReportService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ReportsRestConsumer implements LoansReportService {
  private final WebClient client;

  public ReportsRestConsumer(
      WebClient.Builder builder,
      @Value("${services.reports.url}") String authUrl,
      @Value("${services.auth.api-key}") String apiKey) {
    this.client =
        builder
            .baseUrl(authUrl)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
            .build();
  }

  @Override
  public Mono<LoanReport> getReport() {
    return client.get().retrieve().bodyToMono(LoanReport.class);
  }
}
