package com.co.crediya.requests.r2dbc.repository;

import com.co.crediya.requests.r2dbc.entity.loan.LoanStatusEntity;
import java.util.UUID;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface LoanStatusRepository
    extends ReactiveCrudRepository<LoanStatusEntity, UUID>,
        ReactiveQueryByExampleExecutor<LoanStatusEntity> {
	Mono<LoanStatusEntity> findByName(String name);
}
