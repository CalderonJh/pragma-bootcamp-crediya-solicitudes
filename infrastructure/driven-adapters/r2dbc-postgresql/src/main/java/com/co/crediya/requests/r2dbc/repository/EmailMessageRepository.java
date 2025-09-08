package com.co.crediya.requests.r2dbc.repository;

import com.co.crediya.requests.r2dbc.entity.notifications.EmailMessageEntity;
import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface EmailMessageRepository extends ReactiveCrudRepository<EmailMessageEntity, UUID> {
  Mono<EmailMessageEntity> findByKey(String key);
}
