package com.co.crediya.requests.sqs.listener;

import com.co.crediya.requests.sqs.listener.dto.AssessLoanAplResultDTO;
import com.co.crediya.requests.usecase.loan.UpdateAutoApprovedLoanUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.Message;

@Service
@RequiredArgsConstructor
public class SQSProcessor implements Function<Message, Mono<Void>> {
  private final UpdateAutoApprovedLoanUseCase updateAutoApprovedLoanUseCase;
  private final ObjectMapper objectMapper;

  private static final Logger logger = Logger.getLogger(SQSProcessor.class.getName());

  @Override
  public Mono<Void> apply(Message message) {
    logger.info("Received message at SQSProcessor: " + message.body());
    try {
      AssessLoanAplResultDTO body =
          objectMapper.readValue(message.body(), new TypeReference<>() {});
      return updateAutoApprovedLoanUseCase.execute(
          UUID.fromString(body.applicationId()), body.status()).then();
    } catch (JsonProcessingException e) {
      return Mono.error(e);
    }
  }
}
