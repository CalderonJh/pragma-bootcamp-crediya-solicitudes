package com.co.crediya.requests.model.loanapplication.gateways;

import com.co.crediya.requests.model.loanapplication.User;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserService {
  Mono<User> getUserById(UUID applicantId);

  Flux<User> getByRole(String role);
}
