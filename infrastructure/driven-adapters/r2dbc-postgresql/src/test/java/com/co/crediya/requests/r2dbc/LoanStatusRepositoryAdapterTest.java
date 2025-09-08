package com.co.crediya.requests.r2dbc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.co.crediya.requests.model.loanapplication.LoanStatus;
import com.co.crediya.requests.r2dbc.entity.loan.LoanStatusEntity;
import com.co.crediya.requests.r2dbc.repository.LoanStatusRepository;
import com.co.crediya.requests.r2dbc.repository.adapter.LoanStatusRepositoryAdapter;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class LoanStatusRepositoryAdapterTest {

  @InjectMocks LoanStatusRepositoryAdapter repositoryAdapter;

  @Mock LoanStatusRepository repository;
  @Mock ObjectMapper mapper;

  @Test
  void mustReturnLoanStatusWhenExists() {
    String statusName = "APPROVED";
    LoanStatusEntity entity = new LoanStatusEntity(UUID.randomUUID(), statusName, "");
    LoanStatus status = new LoanStatus(entity.getId(), statusName, "");

    when(repository.findByName(statusName)).thenReturn(Mono.just(entity));
    when(mapper.map(entity, LoanStatus.class)).thenReturn(status);

    Mono<LoanStatus> result = repositoryAdapter.findLoanStatusByName(statusName);

    StepVerifier.create(result)
        .expectNextMatches(s -> s.getName().equals("APPROVED"))
        .verifyComplete();

    verify(mapper).map(entity, LoanStatus.class);
  }

  @Test
  void mustReturnEmptyWhenLoanStatusDoesNotExist() {
    String statusName = "UNKNOWN";

    when(repository.findByName(statusName)).thenReturn(Mono.empty());

    Mono<LoanStatus> result = repositoryAdapter.findLoanStatusByName(statusName);

    StepVerifier.create(result).verifyComplete();

    verify(mapper, never()).map(any(), eq(LoanStatus.class));
  }
}
