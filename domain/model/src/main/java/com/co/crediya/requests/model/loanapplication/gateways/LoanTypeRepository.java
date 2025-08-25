package com.co.crediya.requests.model.loanapplication.gateways;

import com.co.crediya.requests.model.loanapplication.LoanType;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface LoanTypeRepository {
  Mono<LoanType> findLoanTypeById(UUID id);
}
