package com.co.crediya.requests.model.loanapplication.gateways;

import com.co.crediya.requests.model.loanapplication.LoanReport;
import reactor.core.publisher.Mono;

public interface LoansReportService {
  Mono<LoanReport> getReport();
}
