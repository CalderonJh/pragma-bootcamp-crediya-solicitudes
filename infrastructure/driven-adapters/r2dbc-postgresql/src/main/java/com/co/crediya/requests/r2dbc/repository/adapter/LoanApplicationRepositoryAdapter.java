package com.co.crediya.requests.r2dbc.repository.adapter;

import com.co.crediya.requests.model.loanapplication.LoanApplication;
import com.co.crediya.requests.model.loanapplication.LoanStatus;
import com.co.crediya.requests.model.loanapplication.LoanType;
import com.co.crediya.requests.r2dbc.entity.LoanApplicationEntity;
import com.co.crediya.requests.r2dbc.helper.ReactiveAdapterOperations;
import com.co.crediya.requests.r2dbc.mapper.LoanApplicationMapper;
import java.util.UUID;

import com.co.crediya.requests.r2dbc.repository.LoanApplicationRepository;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class LoanApplicationRepositoryAdapter
    extends ReactiveAdapterOperations<
        LoanApplication, LoanApplicationEntity, UUID, LoanApplicationRepository>
    implements com.co.crediya.requests.model.loanapplication.gateways.LoanApplicationRepository {
  public LoanApplicationRepositoryAdapter(
      LoanApplicationRepository repository, ObjectMapper mapper) {
    super(repository, mapper, d -> mapper.map(d, LoanApplication.class));
  }

  @Override
  public Mono<Void> saveLoanApplication(LoanApplication loanApplication) {
    LoanApplicationEntity entity = LoanApplicationMapper.toEntity(loanApplication);
    return repository.save(entity).then();
  }

  @Override
  public Flux<LoanApplication> getLoanApplications() {
    return null;
  }
}
