package com.co.crediya.requests.usecase.loan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.co.crediya.requests.constant.LoanStatusType;
import com.co.crediya.requests.constant.NotifyStatusType;
import com.co.crediya.requests.model.loanapplication.Applicant;
import com.co.crediya.requests.model.loanapplication.LoanApplication;
import com.co.crediya.requests.model.loanapplication.LoanStatus;
import com.co.crediya.requests.model.loanapplication.LoanType;
import com.co.crediya.requests.model.loanapplication.gateways.ApplicantService;
import com.co.crediya.requests.model.loanapplication.gateways.LoanApplicationRepository;
import com.co.crediya.requests.model.loanapplication.gateways.LoanStatusRepository;
import com.co.crediya.requests.model.loanapplication.gateways.UserNotificationService;
import com.co.crediya.requests.model.notifications.EmailMessage;
import com.co.crediya.requests.model.notifications.gateways.EmailMessageRepository;
import com.co.crediya.requests.util.validation.MessageTemplate;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class UpdateAutoApprovedLoanUseCaseTest {
  private LoanApplicationRepository loanApplicationRepository;
  private LoanStatusRepository loanStatusRepository;
  private UserNotificationService userNotificationService;
  private EmailMessageRepository emailMessageRepository;
  private ApplicantService applicantService;

  private UpdateAutoApprovedLoanUseCase useCase;

  @BeforeEach
  void setUp() {
    loanApplicationRepository = mock(LoanApplicationRepository.class);
    loanStatusRepository = mock(LoanStatusRepository.class);
    userNotificationService = mock(UserNotificationService.class);
    emailMessageRepository = mock(EmailMessageRepository.class);
    applicantService = mock(ApplicantService.class);

    useCase =
        new UpdateAutoApprovedLoanUseCase(
            loanApplicationRepository,
            userNotificationService,
            loanStatusRepository,
            applicantService,
            emailMessageRepository);
  }

  @Test
  @DisplayName("Must update loan application status and notify user")
  void mustUpdateLoanApplicationStatusAndNotifyUser() {
    UUID applicationId = UUID.randomUUID();
    UUID applicantId = UUID.randomUUID();

    LoanApplication loanApplication =
        LoanApplication.builder()
            .id(applicationId)
            .loanType(
                LoanType.builder()
                    .description("Personal")
                    .autoValidate(true)
                    .interestRate(BigDecimal.valueOf(0.12))
                    .build())
            .applicantId(applicantId)
            .amount(BigDecimal.valueOf(1000))
            .termInMonths(12)
            .loanStatus(new LoanStatus(UUID.randomUUID(), "PENDING", "Pendiente"))
            .build();

    LoanStatus newStatus =
        new LoanStatus(UUID.randomUUID(), NotifyStatusType.APPROVED.getDbValue(), "Aprobado");
    Applicant applicant =
        new Applicant(applicantId, "John", "Doe", "email@email.com", BigDecimal.TEN);
    EmailMessage emailMessage =
        new EmailMessage(
            UUID.randomUUID(), NotifyStatusType.APPROVED.getMsgKey(), "Subject", "Body", null);

    when(loanApplicationRepository.getById(applicationId)).thenReturn(Mono.just(loanApplication));
    when(loanStatusRepository.findLoanStatusByName(newStatus.getName()))
        .thenReturn(Mono.just(newStatus));
    when(loanApplicationRepository.saveLoanApplication(any()))
        .thenAnswer(inv -> Mono.just(inv.getArgument(0)));
    when(applicantService.getApplicantById(applicantId)).thenReturn(Mono.just(applicant));
    when(emailMessageRepository.getByKey(any())).thenReturn(Mono.just(emailMessage));
    when(userNotificationService.sendNotificationByEmail(eq(applicant), any()))
        .thenReturn(Mono.just("msgId-123"));

    StepVerifier.create(useCase.execute(applicationId, newStatus.getName()))
        .expectNextMatches(
            updated ->
                updated.getLoanStatus().getName().equals(NotifyStatusType.APPROVED.getDbValue()))
        .verifyComplete();

    verify(loanApplicationRepository).getById(applicationId);
    verify(loanStatusRepository).findLoanStatusByName(newStatus.getName());
    verify(loanApplicationRepository).saveLoanApplication(any());
    verify(applicantService).getApplicantById(applicantId);
    verify(emailMessageRepository).getByKey(NotifyStatusType.APPROVED.getMsgKey());
    verify(userNotificationService).sendNotificationByEmail(eq(applicant), any());
  }

  @Test
  @DisplayName("Must fail when loan application not found")
  void mustFailWhenLoanApplicationNotFound() {
    UUID applicationId = UUID.randomUUID();

    when(loanApplicationRepository.getById(applicationId)).thenReturn(Mono.empty());

    StepVerifier.create(useCase.execute(applicationId, "APPROVED"))
        .expectErrorMatches(
            ex ->
                ex instanceof IllegalArgumentException
                    && ex.getMessage().equals(MessageTemplate.NOT_FOUND.render("Loan application")))
        .verify();

    verify(loanApplicationRepository).getById(applicationId);
    verifyNoInteractions(
        loanStatusRepository, applicantService, userNotificationService, emailMessageRepository);
  }

  @Test
  @DisplayName("Must not build payment plan when status is not APPROVED")
  void mustNotBuildPaymentPlanWhenStatusIsNotApproved() {
    UUID applicationId = UUID.randomUUID();
    UUID applicantId = UUID.randomUUID();

    LoanApplication loanApplication =
        LoanApplication.builder()
            .id(applicationId)
            .applicantId(applicantId)
            .loanType(
                LoanType.builder()
                    .description("Personal")
                    .autoValidate(true)
                    .interestRate(BigDecimal.valueOf(0.12))
                    .build())
            .amount(BigDecimal.valueOf(1000))
            .termInMonths(12)
            .loanStatus(
                new LoanStatus(
                    UUID.randomUUID(), LoanStatusType.REJECTED.getDbValue(), "Rechazado"))
            .build();

    Applicant applicant =
        new Applicant(applicantId, "Jane", "Doe", "email@email.com", BigDecimal.TEN);
    EmailMessage emailMessage =
        new EmailMessage(
            UUID.randomUUID(), NotifyStatusType.REJECTED.getMsgKey(), "Subject", "Body", null);

    when(loanApplicationRepository.getById(applicationId)).thenReturn(Mono.just(loanApplication));
    when(loanStatusRepository.findLoanStatusByName(LoanStatusType.REJECTED.getDbValue()))
        .thenReturn(Mono.just(loanApplication.getLoanStatus()));
    when(loanApplicationRepository.saveLoanApplication(any()))
        .thenAnswer(inv -> Mono.just(inv.getArgument(0)));
    when(applicantService.getApplicantById(applicantId)).thenReturn(Mono.just(applicant));
    when(emailMessageRepository.getByKey(NotifyStatusType.REJECTED.getMsgKey()))
        .thenReturn(Mono.just(emailMessage));
    when(userNotificationService.sendNotificationByEmail(eq(applicant), any()))
        .thenReturn(Mono.just("msgId-456"));

    StepVerifier.create(useCase.execute(applicationId, LoanStatusType.REJECTED.getDbValue()))
        .assertNext(
            updated -> {
              assertEquals(LoanStatusType.REJECTED.getDbValue(), updated.getLoanStatus().getName());
              assertTrue(
                  emailMessage.getParams() == null
                      || !emailMessage.getParams().containsKey("installments"),
                  "Installments should not be present when status is not APPROVED");
            })
        .verifyComplete();

    verify(emailMessageRepository).getByKey(NotifyStatusType.REJECTED.getMsgKey());
    verify(userNotificationService).sendNotificationByEmail(eq(applicant), any());
  }
}
