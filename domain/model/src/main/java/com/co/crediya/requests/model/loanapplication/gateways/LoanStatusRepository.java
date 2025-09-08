package com.co.crediya.requests.model.loanapplication.gateways;

import com.co.crediya.requests.model.loanapplication.LoanStatus;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface LoanStatusRepository {
  Mono<LoanStatus> findLoanStatusByName(String name);

	Mono<LoanStatus> getById(UUID statusId);
}
