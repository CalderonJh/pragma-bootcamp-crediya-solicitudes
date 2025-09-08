package com.co.crediya.requests.model.notifications.gateways;

import com.co.crediya.requests.model.notifications.EmailMessage;
import reactor.core.publisher.Mono;

public interface EmailMessageRepository {
	Mono<EmailMessage> getByKey(String key);
}
