package com.co.crediya.requests.scheduler;

import com.co.crediya.requests.usecase.loan.GetDailyReportUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@EnableScheduling
public class DailyReportScheduler {
  private final GetDailyReportUseCase useCase;

  @Scheduled(cron = "0 0 5 * * *")
  public void runDailyTask() {
    useCase.execute().subscribe();
  }
}
