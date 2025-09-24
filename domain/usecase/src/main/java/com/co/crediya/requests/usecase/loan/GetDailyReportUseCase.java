package com.co.crediya.requests.usecase.loan;

import static com.co.crediya.requests.constant.Constant.ADMIN_ROLE;
import static com.co.crediya.requests.constant.Constant.LOAN_DAILY_REPORT_TEMPLATE;

import com.co.crediya.requests.model.loanapplication.User;
import com.co.crediya.requests.model.loanapplication.gateways.LoansReportService;
import com.co.crediya.requests.model.loanapplication.gateways.UserNotificationService;
import com.co.crediya.requests.model.loanapplication.gateways.UserService;
import com.co.crediya.requests.model.notifications.EmailMessage;
import com.co.crediya.requests.model.notifications.gateways.EmailMessageRepository;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class GetDailyReportUseCase {
  private final LoansReportService loansReportService;
  private final UserNotificationService notificationService;
  private final EmailMessageRepository emailMessageRepository;
  private final UserService userService;
  private static final Logger logger = Logger.getLogger(GetDailyReportUseCase.class.getName());

  public Mono<Void> execute() {

    Flux<User> consultants = userService.getByRole(ADMIN_ROLE);
    return getMessageWithParams()
        .flatMap(msg -> notificationService.sendNotificationByEmail(consultants, msg))
        .doOnNext(messageId -> logger.info("Daily report sent with message ID: " + messageId))
        .then();
  }

  public Mono<EmailMessage> getMessageWithParams() {
    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
    return loansReportService
        .getReport()
        .flatMap(
            report ->
                emailMessageRepository
                    .getByKey(LOAN_DAILY_REPORT_TEMPLATE)
                    .map(
                        msg -> {
                          msg.setParams(
                              Map.of(
                                  "reportDate",
                                  new Date().toString(),
                                  "activeLoans",
                                  report.getTotalLoans(),
                                  "totalAmount",
                                  currencyFormat.format(report.getTotalAmount())));
                          return msg;
                        }));
  }
}
