package com.co.crediya.requests.model.loanapplication.gateways;

import com.co.crediya.requests.model.loanapplication.LoanApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LoanApplicationRepository {
  Mono<Void> saveLoanApplication(LoanApplication loanApplication);

  Flux<LoanApplication> getLoanApplications();
}
