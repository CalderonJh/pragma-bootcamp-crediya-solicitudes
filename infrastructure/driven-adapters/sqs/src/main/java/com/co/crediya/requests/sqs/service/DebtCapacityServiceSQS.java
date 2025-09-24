package com.co.crediya.requests.sqs.service;

import com.co.crediya.requests.model.loanapplication.User;
import com.co.crediya.requests.model.loanapplication.LoanApplication;
import com.co.crediya.requests.model.loanapplication.gateways.DebtCapacityService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

@Service
public class DebtCapacityServiceSQS implements DebtCapacityService {
  private static final Logger logger = Logger.getLogger(DebtCapacityServiceSQS.class.getName());
  private static final ObjectMapper mapper = new ObjectMapper();
  private final SqsAsyncClient sqsClient;
  private final String queueUrl;

  public DebtCapacityServiceSQS(
      @Value("${aws.region}") String region,
      @Value("${aws.sqs.debt-capacity-queue-url}") String queueUrl) {
    this.queueUrl = queueUrl;
    this.sqsClient =
        SqsAsyncClient.builder()
            .region(Region.of(region))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
  }

  private Mono<String> sendMessage(String message) {
    logger.info(() -> "Sending debt capacity validation message to SQS: " + message);
    SendMessageRequest request =
        SendMessageRequest.builder().queueUrl(queueUrl).messageBody(message).build();

    return Mono.fromFuture(() -> sqsClient.sendMessage(request))
        .map(SendMessageResponse::messageId);
  }

  @Override
  public Mono<String> validateDebtCapacity(
		User user, List<LoanApplication> activeLoans, LoanApplication currentApplication) {
    try {
      Map<String, Object> payload =
          Map.of(
              "applicant", user,
              "loanApplication", currentApplication,
              "activeLoans", activeLoans);
      String messageBody = mapper.writeValueAsString(payload);
      return sendMessage(messageBody)
          .doOnNext(
              message ->
                  logger.info(() -> "Debt capacity validation message sent with ID: " + message));
    } catch (JsonProcessingException e) {
      return Mono.error(e);
    }
  }
}
