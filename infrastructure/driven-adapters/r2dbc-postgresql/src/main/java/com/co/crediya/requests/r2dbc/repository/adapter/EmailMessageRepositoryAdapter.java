package com.co.crediya.requests.r2dbc.repository.adapter;

import com.co.crediya.requests.model.notifications.EmailMessage;
import com.co.crediya.requests.r2dbc.repository.EmailMessageRepository;
import lombok.AllArgsConstructor;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@AllArgsConstructor
public class EmailMessageRepositoryAdapter
    implements com.co.crediya.requests.model.notifications.gateways.EmailMessageRepository {
  private final EmailMessageRepository repository;
  private final ObjectMapper objectMapper;

  @Override
  public Mono<EmailMessage> getByKey(String key) {
    return repository.findByKey(key).map(ent -> objectMapper.map(ent, EmailMessage.class));
  }
}
