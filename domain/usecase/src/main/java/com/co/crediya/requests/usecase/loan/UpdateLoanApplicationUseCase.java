package com.co.crediya.requests.usecase.loan;

import static com.co.crediya.requests.util.validation.ReactiveValidators.notNull;

import com.co.crediya.requests.constant.NotifyStatusType;
import com.co.crediya.requests.constant.RoleType;
import com.co.crediya.requests.exception.DataNotFoundException;
import com.co.crediya.requests.model.loanapplication.Actor;
import com.co.crediya.requests.model.loanapplication.LoanApplication;
import com.co.crediya.requests.model.loanapplication.gateways.LoanApplicationRepository;
import com.co.crediya.requests.model.loanapplication.gateways.LoanStatusRepository;
import com.co.crediya.requests.model.loanapplication.gateways.UserNotificationService;
import com.co.crediya.requests.model.notifications.gateways.EmailMessageRepository;
import com.co.crediya.requests.util.validation.MessageTemplate;
import com.co.crediya.requests.util.validation.RoleValidator;
import java.util.UUID;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class UpdateLoanApplicationUseCase {
  private final Logger logger = Logger.getLogger(UpdateLoanApplicationUseCase.class.getName());
  private final LoanApplicationRepository loanApplicationRepository;
  private final LoanStatusRepository loanStatusRepository;
  private final UserNotificationService userNotificationService;
  private final EmailMessageRepository emailMessageRepository;

  public Mono<LoanApplication> updateStatus(UUID applicationId, UUID statusId, Actor actor) {
    return validateRole(actor)
        .then(validateInputs(applicationId, statusId))
        .then(Mono.defer(() -> findLoanApplication(applicationId)))
        .flatMap(apl -> updateLoanStatus(apl, statusId))
        .flatMap(
            apl -> {
              String loanStatusName = apl.getLoanStatus().getName();
              if (NotifyStatusType.mustNotify(loanStatusName)) {
                return notifyUser(apl.getApplicantId(), loanStatusName).thenReturn(apl);
              }
              return Mono.just(apl);
            });
  }

  private Mono<String> notifyUser(UUID applicantId, String loanStatusName) {
    return emailMessageRepository
        .getByKey(NotifyStatusType.fromDBValue(loanStatusName).getMsgKey())
        .switchIfEmpty(
            Mono.error(
                new DataNotFoundException(
                    MessageTemplate.NOT_FOUND.render("Email message template" + loanStatusName))))
        .flatMap(
            emailMessage ->
                userNotificationService.sendNotificationByEmail(applicantId, emailMessage))
        .doOnNext(
            msgId ->
                logger.info(
                    () ->
                        "Notify to user %s about application status change to '%s'. Notification sent with message id %s"
                            .formatted(applicantId, loanStatusName, msgId)));
  }

  private Mono<Actor> validateRole(Actor actor) {
    return RoleValidator.hasRole(actor, RoleType.CONSULTANT).thenReturn(actor);
  }

  private Mono<Void> validateInputs(UUID applicationId, UUID statusId) {
    return notNull(applicationId, "Application id").then(notNull(statusId, "Status id")).then();
  }

  private Mono<LoanApplication> findLoanApplication(UUID applicationId) {
    return loanApplicationRepository
        .getById(applicationId)
        .switchIfEmpty(
            Mono.error(
                new DataNotFoundException(MessageTemplate.NOT_FOUND.render("Loan application"))));
  }

  private Mono<LoanApplication> updateLoanStatus(LoanApplication loanApplication, UUID statusId) {
    return loanStatusRepository
        .getById(statusId)
        .switchIfEmpty(
            Mono.error(new DataNotFoundException(MessageTemplate.NOT_FOUND.render("Loan status"))))
        .flatMap(
            loanStatus -> {
              loanApplication.setLoanStatus(loanStatus);
              return loanApplicationRepository.saveLoanApplication(loanApplication);
            });
  }
}
