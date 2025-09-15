package com.co.crediya.requests.model.loanapplication.gateways;

import com.co.crediya.requests.model.loanapplication.LoanApplication;
import com.co.crediya.requests.model.loanapplication.LoanApplicationFilter;
import com.co.crediya.requests.model.util.pagination.Page;
import com.co.crediya.requests.model.util.pagination.Pageable;
import java.util.List;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface LoanApplicationRepository {
  Mono<LoanApplication> saveLoanApplication(LoanApplication loanApplication);

  Mono<LoanApplication> getById(UUID applicationId);

	Mono<Page<LoanApplication>> getLoanApplicationsPage(Pageable pageable, LoanApplicationFilter filter);

  Mono<List<LoanApplication>> getByUserIdAndStatus(UUID userId, String statusName);
}
