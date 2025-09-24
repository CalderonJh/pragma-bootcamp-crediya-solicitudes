package com.co.crediya.requests.usecase.loan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.co.crediya.requests.constant.Constant;
import com.co.crediya.requests.constant.RoleType;
import com.co.crediya.requests.model.loanapplication.LoanReport;
import com.co.crediya.requests.model.loanapplication.User;
import com.co.crediya.requests.model.loanapplication.gateways.LoansReportService;
import com.co.crediya.requests.model.loanapplication.gateways.UserNotificationService;
import com.co.crediya.requests.model.loanapplication.gateways.UserService;
import com.co.crediya.requests.model.notifications.EmailMessage;
import com.co.crediya.requests.model.notifications.gateways.EmailMessageRepository;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class GetDailyReportUseCaseTest {

  private LoansReportService loansReportService;
  private UserNotificationService notificationService;
  private EmailMessageRepository emailMessageRepository;
  private UserService userService;
  private GetDailyReportUseCase useCase;

  @BeforeEach
  void setUp() {
    loansReportService = mock(LoansReportService.class);
    notificationService = mock(UserNotificationService.class);
    emailMessageRepository = mock(EmailMessageRepository.class);
    userService = mock(UserService.class);

    useCase =
        new GetDailyReportUseCase(
            loansReportService, notificationService, emailMessageRepository, userService);
  }

  @Test
  void shouldSendDailyReport() {
    // given
    User consultant = new User(UUID.randomUUID(), "John", "Doe", "email@email.com", BigDecimal.ONE);

    Flux<User> consultants = Flux.just(consultant);

    LoanReport report = new LoanReport(1L, BigDecimal.TEN);
    EmailMessage template =
        new EmailMessage(
            UUID.randomUUID(), "subject", "html", Constant.LOAN_DAILY_REPORT_TEMPLATE, null);

    when(userService.getByRole(RoleType.ADMIN.getValue())).thenReturn(consultants);
    when(loansReportService.getReport()).thenReturn(Mono.just(report));
    when(emailMessageRepository.getByKey(Constant.LOAN_DAILY_REPORT_TEMPLATE))
        .thenReturn(Mono.just(template));
    when(notificationService.sendNotificationByEmail(consultants, template))
        .thenReturn(Mono.just("msg-id-123"));

    // when
    StepVerifier.create(useCase.execute()).verifyComplete();

    // then
    verify(userService).getByRole(RoleType.ADMIN.getValue());
    verify(loansReportService).getReport();
    verify(emailMessageRepository).getByKey(Constant.LOAN_DAILY_REPORT_TEMPLATE);
    verify(notificationService).sendNotificationByEmail(consultants, template);
  }

  @Test
  void shouldBuildMessageWithParams() {
    // given
    LoanReport report = new LoanReport(1L, BigDecimal.TEN);
    EmailMessage template =
        new EmailMessage(
            UUID.randomUUID(), "subject", "html", Constant.LOAN_DAILY_REPORT_TEMPLATE, null);

    when(loansReportService.getReport()).thenReturn(Mono.just(report));
    when(emailMessageRepository.getByKey(Constant.LOAN_DAILY_REPORT_TEMPLATE))
        .thenReturn(Mono.just(template));

    // when
    Mono<EmailMessage> result = useCase.getMessageWithParams();

    // then
    StepVerifier.create(result)
        .assertNext(
            msg -> {
              assertThat(msg.getParams()).containsKeys("reportDate", "activeLoans", "totalAmount");
              assertThat(msg.getParams()).containsEntry("activeLoans", 1L);
              assertThat(msg.getParams().get("reportDate")).isNotNull();
              assertThat(msg.getParams())
                  .containsEntry(
                      "totalAmount",
                      NumberFormat.getCurrencyInstance(Locale.US).format(BigDecimal.TEN));
            })
        .verifyComplete();

    verify(loansReportService).getReport();
    verify(emailMessageRepository).getByKey(Constant.LOAN_DAILY_REPORT_TEMPLATE);
  }
}
