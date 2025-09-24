package com.co.crediya.requests.usecase.loan;

import static com.co.crediya.requests.util.validation.ErrorMessage.LOAN_TYPE_NO_AUTO_APPROVAL;
import static com.co.crediya.requests.util.validation.ReactiveValidators.notNull;
import static reactor.core.publisher.Mono.defer;

import com.co.crediya.requests.constant.LoanStatusType;
import com.co.crediya.requests.constant.NotifyStatusType;
import com.co.crediya.requests.exception.DataNotFoundException;
import com.co.crediya.requests.model.loanapplication.Installment;
import com.co.crediya.requests.model.loanapplication.LoanApplication;
import com.co.crediya.requests.model.loanapplication.User;
import com.co.crediya.requests.model.loanapplication.gateways.*;
import com.co.crediya.requests.model.notifications.EmailMessage;
import com.co.crediya.requests.model.notifications.gateways.EmailMessageRepository;
import com.co.crediya.requests.util.validation.MessageTemplate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class UpdateAutoApprovedLoanUseCase {
  private final LoanApplicationRepository loanApplicationRepository;
  private final UserNotificationService notificationService;
  private final LoanStatusRepository loanStatusRepository;
  private final UserService userService;
  private final EmailMessageRepository emailMessageRepository;
  private final UpdateReportService updateReportService;

  private static final Logger logger =
      Logger.getLogger(UpdateAutoApprovedLoanUseCase.class.getName());

  public Mono<LoanApplication> execute(UUID applicationId, String result) {
    logger.info(() -> "auto-update for applicationId: " + applicationId + " to: " + result);

    return validateParams(applicationId, result)
        .then(defer(() -> getLoanApplication(applicationId)))
        .flatMap(
            apl -> {
              LoanStatusType status = LoanStatusType.fromDBValue(result);
              if (apl.getLoanStatus().getName().equals(status.getDbValue())) {
                return Mono.just(apl);
              }
              return validateLoanType(apl)
                  .flatMap(a -> updateLoanApplication(a, status))
                  .flatMap(this::updateActiveLoansReport)
                  .zipWhen(this::getApplicant)
                  .flatMap(t -> notifyUser(t.getT1(), t.getT2()));
            });
  }

  private Mono<Void> validateParams(UUID applicationId, String result) {
    return notNull(applicationId, "Loan application id").then(notNull(result, "Result"));
  }

  private Mono<User> getApplicant(LoanApplication application) {
    return userService
        .getUserById(application.getApplicantId())
        .switchIfEmpty(
            Mono.error(new DataNotFoundException(MessageTemplate.NOT_FOUND.render("Applicant"))));
  }

  private Mono<LoanApplication> updateLoanApplication(
      LoanApplication loanApplication, LoanStatusType newStatus) {
    return loanStatusRepository
        .findLoanStatusByName(newStatus.getDbValue())
        .switchIfEmpty(
            Mono.error(new DataNotFoundException(MessageTemplate.NOT_FOUND.render("Loan status"))))
        .flatMap(
            status -> {
              loanApplication.setLoanStatus(status);
              return loanApplicationRepository.saveLoanApplication(loanApplication);
            });
  }

  private Mono<LoanApplication> getLoanApplication(UUID applicationId) {
    return loanApplicationRepository
        .getById(applicationId)
        .switchIfEmpty(
            Mono.error(
                new DataNotFoundException(MessageTemplate.NOT_FOUND.render("Loan application"))));
  }

  private Mono<LoanApplication> validateLoanType(LoanApplication loanApplication) {
    return Boolean.TRUE.equals(loanApplication.getLoanType().getAutoValidate())
        ? Mono.just(loanApplication)
        : Mono.error(new IllegalArgumentException(LOAN_TYPE_NO_AUTO_APPROVAL));
  }

  private Mono<LoanApplication> notifyUser(LoanApplication loanApplication, User user) {
    NotifyStatusType statusType =
        NotifyStatusType.fromDBValue(loanApplication.getLoanStatus().getName());
    return emailMessageRepository
        .getByKey(statusType.getMsgKey())
        .switchIfEmpty(
            Mono.error(
                new DataNotFoundException(
                    MessageTemplate.NOT_FOUND.render("Email message template"))))
        .flatMap(msg -> resolveMessageParams(loanApplication, msg, user))
        .flatMap(msg -> notificationService.sendNotificationByEmail(user, msg))
        .thenReturn(loanApplication);
  }

  private Mono<EmailMessage> resolveMessageParams(
      LoanApplication loanApplication, EmailMessage message, User user) {
    NotifyStatusType statusType =
        NotifyStatusType.fromDBValue(loanApplication.getLoanStatus().getName());
    Map<String, Object> params = new HashMap<>();
    params.put("nombre", user.getName());
    if (statusType == NotifyStatusType.APPROVED) {
      params.put("installments", buildPaymentPlan(loanApplication));
    }
    message.setParams(params);
    return Mono.just(message);
  }

  private List<Installment> buildPaymentPlan(LoanApplication application) {
    BigDecimal principal = application.getAmount();
    BigDecimal annualRate = application.getLoanType().getInterestRate();
    int termMonths = application.getTermInMonths();

    // Convert annual rate to monthly
    BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

    // Cuota fija con fórmula de amortización
    BigDecimal onePlusI = monthlyRate.add(BigDecimal.ONE);
    BigDecimal numerator = monthlyRate.multiply(onePlusI.pow(termMonths));
    BigDecimal denominator = onePlusI.pow(termMonths).subtract(BigDecimal.ONE);
    BigDecimal fixedInstallment =
        principal.multiply(numerator).divide(denominator, 2, RoundingMode.HALF_UP);

    List<Installment> installments = new ArrayList<>();
    BigDecimal remaining = principal;
    LocalDate now = LocalDate.now();
    for (int n = 0; n < termMonths; n++) {
      BigDecimal interest = remaining.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
      BigDecimal capital = fixedInstallment.subtract(interest).setScale(2, RoundingMode.HALF_UP);
      remaining = remaining.subtract(capital).setScale(2, RoundingMode.HALF_UP);

      LocalDate dueDate = now.plusMonths(n);

      installments.add(
          new Installment(
              n + 1,
              dueDate.format(DateTimeFormatter.ISO_DATE),
              capital.toString(),
              interest.toString(),
              fixedInstallment.toString()));
    }

    return installments;
  }

  private Mono<LoanApplication> updateActiveLoansReport(LoanApplication loanApplication) {
    if (LoanStatusType.APPROVED.getDbValue().equals(loanApplication.getLoanStatus().getName()))
      return updateReportService
          .update(1L, loanApplication.getAmount())
          .flatMap(msgId -> Mono.just(loanApplication));
    else return Mono.just(loanApplication);
  }
}
