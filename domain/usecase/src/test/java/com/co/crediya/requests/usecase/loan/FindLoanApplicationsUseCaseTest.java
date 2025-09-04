package com.co.crediya.requests.usecase.loan;

import static org.mockito.Mockito.*;

import com.co.crediya.requests.constant.RoleType;
import com.co.crediya.requests.exception.PermissionException;
import com.co.crediya.requests.model.loanapplication.Actor;
import com.co.crediya.requests.model.loanapplication.LoanApplicationFilter;
import com.co.crediya.requests.model.loanapplication.gateways.LoanApplicationRepository;
import com.co.crediya.requests.model.util.pagination.Pageable;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class FindLoanApplicationsUseCaseTest {
  private FindLoanApplicationsUseCase useCase;
  private LoanApplicationRepository loanApplicationRepository;

  @BeforeEach
  void setUp() {
    loanApplicationRepository = mock(LoanApplicationRepository.class);
    useCase = new FindLoanApplicationsUseCase(loanApplicationRepository);
  }

  @Test
  void shouldReturnPageWhenActorHasRole() {
    Pageable pageable = Pageable.of(0, 10);
    LoanApplicationFilter filter = new LoanApplicationFilter(UUID.randomUUID());
    Actor actor = new Actor(UUID.randomUUID(), RoleType.ADMIN.getValue()); // valid role

    when(loanApplicationRepository.getLoanApplicationsPage(pageable, filter))
        .thenReturn(Mono.empty());

    StepVerifier.create(useCase.execute(pageable, filter, actor)).verifyComplete();

    verify(loanApplicationRepository).getLoanApplicationsPage(pageable, filter);
  }

  @Test
  void shouldFailWhenActorHasNoRole() {
    Pageable pageable = Pageable.of(0, 10);
    LoanApplicationFilter filter = new LoanApplicationFilter(UUID.randomUUID());
    Actor actor = new Actor(UUID.randomUUID(), RoleType.USER.getValue());

    StepVerifier.create(useCase.execute(pageable, filter, actor))
        .expectError(PermissionException.class)
        .verify();

    verify(loanApplicationRepository, never()).getLoanApplicationsPage(any(), any());
  }
}
