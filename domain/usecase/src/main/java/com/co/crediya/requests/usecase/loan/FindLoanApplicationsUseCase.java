package com.co.crediya.requests.usecase.loan;

import com.co.crediya.requests.constant.RoleType;
import com.co.crediya.requests.model.loanapplication.Actor;
import com.co.crediya.requests.model.loanapplication.LoanApplication;
import com.co.crediya.requests.model.loanapplication.LoanApplicationFilter;
import com.co.crediya.requests.model.loanapplication.gateways.LoanApplicationRepository;
import com.co.crediya.requests.model.util.pagination.Page;
import com.co.crediya.requests.model.util.pagination.Pageable;
import com.co.crediya.requests.util.validation.RoleValidator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class FindLoanApplicationsUseCase {
  private final LoanApplicationRepository loanApplicationRepository;

  public Mono<Page<LoanApplication>> execute(Pageable pageable, LoanApplicationFilter filter, Actor actor) {
    return RoleValidator.hasRole(actor, RoleType.ADMIN, RoleType.CONSULTANT)
        .then(Mono.defer(() -> loanApplicationRepository.getLoanApplicationsPage(pageable, filter)));
  }
}
