package com.co.crediya.requests.r2dbc.repository.adapter;

import com.co.crediya.requests.model.loanapplication.LoanType;
import com.co.crediya.requests.r2dbc.entity.LoanTypeEntity;
import com.co.crediya.requests.r2dbc.helper.ReactiveAdapterOperations;
import com.co.crediya.requests.r2dbc.repository.LoanTypeRepository;
import java.util.UUID;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class LoanTypeRepositoryAdapter
    extends ReactiveAdapterOperations<LoanType, LoanTypeEntity, UUID, LoanTypeRepository>
    implements com.co.crediya.requests.model.loanapplication.gateways.LoanTypeRepository {

  public LoanTypeRepositoryAdapter(LoanTypeRepository repository, ObjectMapper mapper) {
    super(repository, mapper, d -> mapper.map(d, LoanType.class));
  }

  @Override
  public Mono<LoanType> findLoanTypeById(UUID id) {
    return super.findById(id);
  }
}
