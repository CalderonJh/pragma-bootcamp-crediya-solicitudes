package com.co.crediya.requests.usecase.loan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.co.crediya.requests.constant.LoanStatusType;
import com.co.crediya.requests.constant.NotifyStatusType;
import com.co.crediya.requests.exception.DataNotFoundException;
import com.co.crediya.requests.model.loanapplication.User;
import com.co.crediya.requests.model.loanapplication.LoanApplication;
import com.co.crediya.requests.model.loanapplication.LoanStatus;
import com.co.crediya.requests.model.loanapplication.LoanType;
import com.co.crediya.requests.model.loanapplication.gateways.*;
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

  private static final UUID DEFAULT_APPLICATION_ID = UUID.randomUUID();
  private static final UUID DEFAULT_APPLICANT_ID = UUID.randomUUID();
  private static final BigDecimal DEFAULT_AMOUNT = BigDecimal.valueOf(1000);
  private static final int DEFAULT_TERM = 12;

  private static final LoanStatus PENDING_STATUS =
      new LoanStatus(UUID.randomUUID(), LoanStatusType.PENDING.getDbValue(), "Pendiente");
  private static final LoanStatus APPROVED_STATUS =
      new LoanStatus(UUID.randomUUID(), NotifyStatusType.APPROVED.getDbValue(), "Aprobado");
  private static final LoanStatus REJECTED_STATUS =
      new LoanStatus(UUID.randomUUID(), LoanStatusType.REJECTED.getDbValue(), "Rechazado");

  private LoanApplicationRepository loanApplicationRepository;
  private LoanStatusRepository loanStatusRepository;
  private UserNotificationService userNotificationService;
  private EmailMessageRepository emailMessageRepository;
  private UserService userService;
  private UpdateReportService updateReportService;
  private UpdateAutoApprovedLoanUseCase useCase;

  private LoanApplication baseLoanApplication;

  @BeforeEach
  void setUp() {
    loanApplicationRepository = mock(LoanApplicationRepository.class);
    loanStatusRepository = mock(LoanStatusRepository.class);
    userNotificationService = mock(UserNotificationService.class);
    emailMessageRepository = mock(EmailMessageRepository.class);
    userService = mock(UserService.class);
    updateReportService = mock(UpdateReportService.class);

    useCase =
        new UpdateAutoApprovedLoanUseCase(
            loanApplicationRepository,
            userNotificationService,
            loanStatusRepository,
					userService,
            emailMessageRepository,
            updateReportService);

    baseLoanApplication =
        LoanApplication.builder()
            .id(DEFAULT_APPLICATION_ID)
            .applicantId(DEFAULT_APPLICANT_ID)
            .loanType(
                LoanType.builder()
                    .description("Personal")
                    .autoValidate(true)
                    .interestRate(BigDecimal.valueOf(0.12))
                    .build())
            .amount(DEFAULT_AMOUNT)
            .termInMonths(DEFAULT_TERM)
            .loanStatus(PENDING_STATUS)
            .build();
  }

  private User mockApplicant() {
    return new User(DEFAULT_APPLICANT_ID, "John", "Doe", "email@email.com", BigDecimal.TEN);
  }

  private EmailMessage mockEmailMessage(NotifyStatusType statusType) {
    return new EmailMessage(UUID.randomUUID(), "Subject", "</html>", statusType.getMsgKey(), null);
  }

  private void mockCommonInteractions(
      LoanApplication loanApplication,
      LoanStatus newStatus,
      User user,
      EmailMessage emailMessage) {
    when(loanApplicationRepository.getById(loanApplication.getId()))
        .thenReturn(Mono.just(loanApplication));
    when(loanStatusRepository.findLoanStatusByName(newStatus.getName()))
        .thenReturn(Mono.just(newStatus));
    when(loanApplicationRepository.saveLoanApplication(any()))
        .thenAnswer(inv -> Mono.just(inv.getArgument(0)));
    when(userService.getUserById(loanApplication.getApplicantId()))
        .thenReturn(Mono.just(user));
    when(emailMessageRepository.getByKey(emailMessage.getKey()))
        .thenReturn(Mono.just(emailMessage));
    when(userNotificationService.sendNotificationByEmail(eq(user), any()))
        .thenReturn(Mono.just("msgId-123"));
  }

  @Test
  @DisplayName("Must update loan application status, notify user and update active loans report")
  void mustUpdateLoanApplicationStatusAndNotifyUser() {
    LoanApplication loanApplication = baseLoanApplication.toBuilder().build();
    User user = mockApplicant();
    EmailMessage emailMessage = mockEmailMessage(NotifyStatusType.APPROVED);

    mockCommonInteractions(loanApplication, APPROVED_STATUS, user, emailMessage);
    when(updateReportService.update(anyLong(), any())).thenReturn(Mono.just("report-ok"));

    StepVerifier.create(useCase.execute(DEFAULT_APPLICATION_ID, APPROVED_STATUS.getName()))
        .expectNextMatches(
            updated ->
                updated.getLoanStatus().getName().equals(NotifyStatusType.APPROVED.getDbValue()))
        .verifyComplete();

    verify(updateReportService).update(1L, DEFAULT_AMOUNT);
  }

  @Test
  @DisplayName("Must fail when loan application not found")
  void mustFailWhenLoanApplicationNotFound() {
    UUID randomId = UUID.randomUUID();
    when(loanApplicationRepository.getById(randomId)).thenReturn(Mono.empty());

    StepVerifier.create(useCase.execute(randomId, APPROVED_STATUS.getName()))
        .expectErrorMatches(
            ex ->
                ex instanceof DataNotFoundException
                    && ex.getMessage().equals(MessageTemplate.NOT_FOUND.render("Loan application")))
        .verify();

    verify(loanApplicationRepository).getById(randomId);
    verifyNoInteractions(
        loanStatusRepository,
			userService,
        userNotificationService,
        emailMessageRepository,
        updateReportService);
  }

  @Test
  @DisplayName("Must not build payment plan when status is not APPROVED")
  void mustNotBuildPaymentPlanWhenStatusIsNotApproved() {
    LoanApplication loanApplication =
        baseLoanApplication.toBuilder().loanStatus(PENDING_STATUS).build();
    User user = mockApplicant();
    EmailMessage emailMessage = mockEmailMessage(NotifyStatusType.REJECTED);

    mockCommonInteractions(loanApplication, REJECTED_STATUS, user, emailMessage);

    StepVerifier.create(useCase.execute(DEFAULT_APPLICATION_ID, REJECTED_STATUS.getName()))
        .assertNext(
            updated -> {
              assertEquals(LoanStatusType.REJECTED.getDbValue(), updated.getLoanStatus().getName());
              assertTrue(
                  emailMessage.getParams() == null
                      || !emailMessage.getParams().containsKey("installments"),
                  "Installments should not be present when status is not APPROVED");
            })
        .verifyComplete();

    verifyNoInteractions(updateReportService);
  }

  @Test
  @DisplayName("Must return same loan when status is already up to date")
  void mustReturnSameLoanWhenStatusAlreadyUpToDate() {
    LoanApplication loanApplication =
        baseLoanApplication.toBuilder()
            .loanStatus(APPROVED_STATUS)
            .amount(BigDecimal.valueOf(2000))
            .termInMonths(6)
            .build();

    when(loanApplicationRepository.getById(DEFAULT_APPLICATION_ID))
        .thenReturn(Mono.just(loanApplication));

    StepVerifier.create(useCase.execute(DEFAULT_APPLICATION_ID, APPROVED_STATUS.getName()))
        .expectNext(loanApplication)
        .verifyComplete();

    verify(loanApplicationRepository).getById(DEFAULT_APPLICATION_ID);
    verifyNoInteractions(
        loanStatusRepository,
			userService,
        emailMessageRepository,
        userNotificationService,
        updateReportService);
  }
}