package com.co.crediya.requests.sqs.service;

import com.co.crediya.requests.api.client.AuthServiceClient;
import com.co.crediya.requests.model.loanapplication.Applicant;
import com.co.crediya.requests.model.loanapplication.gateways.UserNotificationService;
import com.co.crediya.requests.model.notifications.EmailMessage;
import com.co.crediya.requests.sqs.util.EmailRenderer;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

@Service
public class UserNotificationSES implements UserNotificationService {
  private static final ObjectMapper mapper = new ObjectMapper();
  private final AuthServiceClient authServiceClient;
  private final SqsAsyncClient sqsClient;
  private final String queueUrl;

  public UserNotificationSES(
      @Value("${aws.region}") String region,
      @Value("${aws.sqs.notifications-queue-url}") String queueUrl,
      AuthServiceClient authServiceClient) {
    this.authServiceClient = authServiceClient;
    this.queueUrl = queueUrl;
    this.sqsClient =
        SqsAsyncClient.builder()
            .region(Region.of(region))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
  }

  private Mono<String> sendMessage(String message) {
    SendMessageRequest request =
        SendMessageRequest.builder().queueUrl(queueUrl).messageBody(message).build();

    return Mono.fromFuture(() -> sqsClient.sendMessage(request)).map(SendMessageResponse::messageId);
  }

  @Override
  public Mono<String> sendNotificationByEmail(UUID userId, EmailMessage message) {
    return this.authServiceClient
        .getUser(userId)
        .flatMap(
            user -> {
              try {
                String messageBody =
                    mapper.writeValueAsString(
                        Map.of(
                            "toAddress", user.email(),
                            "subject", message.getSubject(),
                            "body", message.getTemplate()));
                return sendMessage(messageBody);
              } catch (Exception e) {
                return Mono.error(e);
              }
            });
  }

  @Override
  public Mono<String> sendNotificationByEmail(Applicant applicant, EmailMessage message) {
    try {
      EmailRenderer renderer = new EmailRenderer();
      String renderedBody = renderer.render(message.getTemplate(), message.getParams());
      String messageBody =
          mapper.writeValueAsString(
              Map.of(
                  "toAddress", applicant.getEmail(),
                  "subject", message.getSubject(),
                  "body", renderedBody));
      return sendMessage(messageBody);
    } catch (Exception e) {
      return Mono.error(e);
    }
  }
}
