package com.co.crediya.requests.r2dbc.repository.adapter;

import com.co.crediya.requests.model.loanapplication.LoanStatus;
import com.co.crediya.requests.r2dbc.entity.loan.LoanStatusEntity;
import com.co.crediya.requests.r2dbc.helper.ReactiveAdapterOperations;
import com.co.crediya.requests.r2dbc.repository.LoanStatusRepository;
import java.util.UUID;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class LoanStatusRepositoryAdapter
    extends ReactiveAdapterOperations<LoanStatus, LoanStatusEntity, UUID, LoanStatusRepository>
    implements com.co.crediya.requests.model.loanapplication.gateways.LoanStatusRepository {

  public LoanStatusRepositoryAdapter(LoanStatusRepository repository, ObjectMapper mapper) {
    super(repository, mapper, d -> mapper.map(d, LoanStatus.class));
  }

  @Override
  public Mono<LoanStatus> findLoanStatusByName(String name) {
    return repository.findByName(name).map(e -> mapper.map(e, LoanStatus.class));
  }

	@Override
	public Mono<LoanStatus> getById(UUID statusId) {
    return repository.findById(statusId).map(e -> mapper.map(e, LoanStatus.class));
	}
}
