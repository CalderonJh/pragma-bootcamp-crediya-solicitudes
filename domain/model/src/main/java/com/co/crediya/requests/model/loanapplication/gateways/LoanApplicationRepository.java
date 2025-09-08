package com.co.crediya.requests.model.loanapplication.gateways;

import com.co.crediya.requests.model.loanapplication.LoanApplication;
import com.co.crediya.requests.model.loanapplication.LoanApplicationFilter;
import com.co.crediya.requests.model.util.pagination.Page;
import com.co.crediya.requests.model.util.pagination.Pageable;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface LoanApplicationRepository {
  Mono<LoanApplication> saveLoanApplication(LoanApplication loanApplication);

  Mono<LoanApplication> getById(UUID applicationId);

	Mono<Page<LoanApplication>> getLoanApplicationsPage(Pageable pageable, LoanApplicationFilter filter);
}
