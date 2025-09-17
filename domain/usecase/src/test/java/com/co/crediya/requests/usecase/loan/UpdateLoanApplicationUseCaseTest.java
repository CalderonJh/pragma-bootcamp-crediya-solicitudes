package com.co.crediya.requests.usecase.loan;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.co.crediya.requests.constant.LoanStatusType;
import com.co.crediya.requests.constant.NotifyStatusType;
import com.co.crediya.requests.constant.RoleType;
import com.co.crediya.requests.exception.DataNotFoundException;
import com.co.crediya.requests.exception.PermissionException;
import com.co.crediya.requests.model.loanapplication.LoanApplication;
import com.co.crediya.requests.model.loanapplication.LoanStatus;
import com.co.crediya.requests.model.loanapplication.gateways.LoanApplicationRepository;
import com.co.crediya.requests.model.loanapplication.gateways.LoanStatusRepository;
import com.co.crediya.requests.model.loanapplication.gateways.UpdateReportService;
import com.co.crediya.requests.model.loanapplication.gateways.UserNotificationService;
import com.co.crediya.requests.model.notifications.EmailMessage;
import com.co.crediya.requests.model.notifications.gateways.EmailMessageRepository;
import com.co.crediya.requests.model.util.Actor;
import com.co.crediya.requests.util.validation.MessageTemplate;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class UpdateLoanApplicationUseCaseTest {
  private LoanApplicationRepository loanApplicationRepository;
  private LoanStatusRepository loanStatusRepository;
  private UserNotificationService userNotificationService;
  private EmailMessageRepository emailMessageRepository;
  private UpdateLoanApplicationUseCase useCase;
  private UpdateReportService updateReportService;

  @BeforeEach
  void setUp() {
    loanApplicationRepository = mock(LoanApplicationRepository.class);
    loanStatusRepository = mock(LoanStatusRepository.class);
    userNotificationService = mock(UserNotificationService.class);
    emailMessageRepository = mock(EmailMessageRepository.class);
    updateReportService = mock(UpdateReportService.class);
    useCase =
        new UpdateLoanApplicationUseCase(
            loanApplicationRepository,
            loanStatusRepository,
            userNotificationService,
            emailMessageRepository,
            updateReportService);
  }

  @Test
  void mustUpdateLoanApplicationStatusAndNotifyUser() {
    UUID applicationId = UUID.randomUUID();
    UUID statusId = UUID.randomUUID();
    UUID applicantId = UUID.randomUUID();

    LoanApplication loanApplication =
        LoanApplication.builder()
            .id(applicationId)
            .applicantId(applicantId)
            .loanStatus(new LoanStatus(UUID.randomUUID(), "PENDING", "Pendiente"))
            .amount(BigDecimal.valueOf(1000))
            .build();

    LoanStatus newStatus =
        new LoanStatus(statusId, NotifyStatusType.APPROVED.getDbValue(), "Aprobado");
    EmailMessage emailMessage =
        new EmailMessage(UUID.randomUUID(), "msgKey", "Subject", "Body", null);

    when(loanApplicationRepository.getById(applicationId)).thenReturn(Mono.just(loanApplication));
    when(loanStatusRepository.getById(statusId)).thenReturn(Mono.just(newStatus));
    when(loanApplicationRepository.saveLoanApplication(any()))
        .thenAnswer(inv -> Mono.just(inv.getArgument(0)));
    when(emailMessageRepository.getByKey(any())).thenReturn(Mono.just(emailMessage));
    when(userNotificationService.sendNotificationByEmail(eq(applicantId), any()))
        .thenReturn(Mono.just("msgId-123"));
    when(updateReportService.update(anyLong(), any())).thenReturn(Mono.just("report-updated"));

    Actor actor = new Actor(UUID.randomUUID(), RoleType.CONSULTANT.getValue());

    StepVerifier.create(useCase.execute(applicationId, statusId, actor))
        .expectNextMatches(
            updated ->
                updated.getLoanStatus().getId().equals(statusId)
                    && NotifyStatusType.APPROVED
                        .getDbValue()
                        .equals(updated.getLoanStatus().getName()))
        .verifyComplete();

    verify(loanApplicationRepository).getById(applicationId);
    verify(loanStatusRepository).getById(statusId);
    verify(loanApplicationRepository).saveLoanApplication(any());
    verify(emailMessageRepository).getByKey(NotifyStatusType.APPROVED.getMsgKey());
    verify(userNotificationService).sendNotificationByEmail(eq(applicantId), any());
    verify(updateReportService).update(1L, loanApplication.getAmount());
  }

  @Test
  void mustFailWhenLoanApplicationNotFound() {
    UUID applicationId = UUID.randomUUID();
    UUID statusId = UUID.randomUUID();
    Actor actor = new Actor(UUID.randomUUID(), RoleType.CONSULTANT.getValue());

    when(loanApplicationRepository.getById(applicationId)).thenReturn(Mono.empty());

    StepVerifier.create(useCase.execute(applicationId, statusId, actor))
        .expectErrorMatches(
            ex ->
                ex instanceof DataNotFoundException
                    && ex.getMessage().equals(MessageTemplate.NOT_FOUND.render("Loan application")))
        .verify();

    verify(loanApplicationRepository).getById(applicationId);
    verifyNoInteractions(loanStatusRepository, userNotificationService, emailMessageRepository);
  }

  @Test
  void mustFailWhenLoanStatusNotFound() {
    UUID applicationId = UUID.randomUUID();
    UUID statusId = UUID.randomUUID();
    UUID applicantId = UUID.randomUUID();
    Actor actor = new Actor(UUID.randomUUID(), RoleType.CONSULTANT.getValue());

    LoanApplication loanApplication =
        LoanApplication.builder()
            .id(applicationId)
            .loanStatus(
                new LoanStatus(UUID.randomUUID(), LoanStatusType.PENDING.getDbValue(), "Pendiente"))
            .applicantId(applicantId)
            .build();

    when(loanApplicationRepository.getById(applicationId)).thenReturn(Mono.just(loanApplication));
    when(loanStatusRepository.getById(statusId)).thenReturn(Mono.empty());

    StepVerifier.create(useCase.execute(applicationId, statusId, actor))
        .expectErrorMatches(
            ex ->
                ex instanceof DataNotFoundException
                    && ex.getMessage().equals(MessageTemplate.NOT_FOUND.render("Loan status")))
        .verify();

    verify(loanApplicationRepository).getById(applicationId);
    verify(loanStatusRepository).getById(statusId);
    verifyNoMoreInteractions(
        loanApplicationRepository,
        userNotificationService,
        emailMessageRepository,
        updateReportService);
  }

  @Test
  void mustFailWhenEmailMessageTemplateNotFound() {
    UUID applicationId = UUID.randomUUID();
    UUID statusId = UUID.randomUUID();
    UUID applicantId = UUID.randomUUID();
    Actor actor = new Actor(UUID.randomUUID(), RoleType.CONSULTANT.getValue());

    LoanApplication loanApplication =
        LoanApplication.builder()
            .id(applicationId)
            .applicantId(applicantId)
            .loanStatus(
                new LoanStatus(UUID.randomUUID(), LoanStatusType.PENDING.getDbValue(), "Pendiente"))
            .amount(BigDecimal.TEN)
            .build();
    LoanStatus newStatus =
        new LoanStatus(statusId, NotifyStatusType.APPROVED.getDbValue(), "Aprobado");

    when(loanApplicationRepository.getById(applicationId)).thenReturn(Mono.just(loanApplication));
    when(loanStatusRepository.getById(statusId)).thenReturn(Mono.just(newStatus));
    when(loanApplicationRepository.saveLoanApplication(any()))
        .thenAnswer(inv -> Mono.just(inv.getArgument(0)));
    when(emailMessageRepository.getByKey(any())).thenReturn(Mono.empty());

    StepVerifier.create(useCase.execute(applicationId, statusId, actor))
        .expectErrorMatches(
            ex ->
                ex instanceof DataNotFoundException
                    && ex.getMessage()
                        .equals(MessageTemplate.NOT_FOUND.render("Email message template")))
        .verify();

    verify(loanApplicationRepository).saveLoanApplication(any());
    verify(emailMessageRepository).getByKey(NotifyStatusType.APPROVED.getMsgKey());
    verifyNoInteractions(userNotificationService, updateReportService);
  }

  @Test
  void mustFailWhenActorDoesNotHaveRequiredRole() {
    UUID applicationId = UUID.randomUUID();
    UUID statusId = UUID.randomUUID();
    Actor actor = new Actor(UUID.randomUUID(), RoleType.USER.getValue()); // not CONSULTANT

    StepVerifier.create(useCase.execute(applicationId, statusId, actor))
        .expectError(PermissionException.class)
        .verify();

    verifyNoInteractions(
        loanApplicationRepository,
        loanStatusRepository,
        userNotificationService,
        emailMessageRepository);
  }
}
