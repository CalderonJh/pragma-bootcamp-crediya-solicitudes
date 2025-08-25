package com.co.crediya.requests.r2dbc.repository;

import com.co.crediya.requests.r2dbc.entity.LoanTypeEntity;
import java.util.UUID;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface LoanTypeRepository
    extends ReactiveCrudRepository<LoanTypeEntity, UUID>,
        ReactiveQueryByExampleExecutor<LoanTypeEntity> {}
