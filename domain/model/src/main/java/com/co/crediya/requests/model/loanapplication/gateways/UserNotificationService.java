package com.co.crediya.requests.model.loanapplication.gateways;


import com.co.crediya.requests.model.notifications.EmailMessage;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserNotificationService {
  Mono<String> sendNotificationByEmail(UUID userId, EmailMessage message);
}
