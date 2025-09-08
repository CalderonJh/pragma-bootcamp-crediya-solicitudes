package com.co.crediya.requests.r2dbc;

import static org.mockito.Mockito.*;

import com.co.crediya.requests.model.loanapplication.LoanType;
import com.co.crediya.requests.r2dbc.entity.loan.LoanTypeEntity;
import com.co.crediya.requests.r2dbc.repository.LoanTypeRepository;
import com.co.crediya.requests.r2dbc.repository.adapter.LoanTypeRepositoryAdapter;
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
class LoanTypeRepositoryAdapterTest {

  @InjectMocks LoanTypeRepositoryAdapter adapter;

  @Mock LoanTypeRepository repository;
  @Mock ObjectMapper mapper;

  @Test
  void shouldFindLoanTypeById() {
    UUID id = UUID.randomUUID();
    LoanType loanType = new LoanType(id);
		LoanTypeEntity entity = new LoanTypeEntity(id);

    // mock mapping + repo call
    when(repository.findById(id)).thenReturn(Mono.just(entity));
    when(mapper.map(entity, LoanType.class)).thenReturn(loanType);

    StepVerifier.create(adapter.findLoanTypeById(id)).expectNext(loanType).verifyComplete();

    verify(repository).findById(id);
    verify(mapper).map(entity, LoanType.class);
  }
}
