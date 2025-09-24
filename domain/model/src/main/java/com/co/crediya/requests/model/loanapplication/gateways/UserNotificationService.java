package com.co.crediya.requests.model.loanapplication.gateways;


import com.co.crediya.requests.model.loanapplication.User;
import com.co.crediya.requests.model.notifications.EmailMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserNotificationService {
  Mono<String> sendNotificationByEmail(UUID userId, EmailMessage message);
  Mono<String> sendNotificationByEmail(User user, EmailMessage message);
	Mono<String> sendNotificationByEmail(Flux<User> users, EmailMessage message);
}
