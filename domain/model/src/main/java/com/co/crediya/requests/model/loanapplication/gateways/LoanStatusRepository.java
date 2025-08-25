package com.co.crediya.requests.model.loanapplication.gateways;

import com.co.crediya.requests.model.loanapplication.LoanStatus;
import reactor.core.publisher.Mono;

public interface LoanStatusRepository {
  Mono<LoanStatus> findLoanStatusByName(String name);
}
