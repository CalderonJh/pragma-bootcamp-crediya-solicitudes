package com.co.crediya.requests.sqs.service;

import com.co.crediya.requests.model.loanapplication.gateways.UpdateReportService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
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
public class UpdateReportServiceSQS implements UpdateReportService {
  private static final Logger logger = Logger.getLogger(UpdateReportServiceSQS.class.getName());
  private static final ObjectMapper mapper = new ObjectMapper();
  private final SqsAsyncClient sqsClient;
  private final String queueUrl;

  public UpdateReportServiceSQS(
      @Value("${aws.region}") String region,
      @Value("${aws.sqs.loan-report-queue-url}") String queueUrl) {
    this.queueUrl = queueUrl;
    this.sqsClient =
        SqsAsyncClient.builder()
            .region(Region.of(region))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
  }

  @Override
  public Mono<String> update(long newLoansCount, BigDecimal totalAmount) {
    try {
      Map<String, Object> payload =
          Map.of(
              "newLoansCount", newLoansCount,
              "totalAmount", totalAmount);
      String messageBody = mapper.writeValueAsString(payload);
      return sendMessage(messageBody)
          .doOnNext(
              message ->
                  logger.info(() -> "Update active loans report message sent with ID: " + message));
    } catch (JsonProcessingException e) {
      return Mono.error(e);
    }
  }

  private Mono<String> sendMessage(String message) {
    logger.info(() -> "Sending update loan report message to SQS: " + message);
    SendMessageRequest request =
        SendMessageRequest.builder().queueUrl(queueUrl).messageBody(message).build();

    return Mono.fromFuture(() -> sqsClient.sendMessage(request))
        .map(SendMessageResponse::messageId);
  }
}
