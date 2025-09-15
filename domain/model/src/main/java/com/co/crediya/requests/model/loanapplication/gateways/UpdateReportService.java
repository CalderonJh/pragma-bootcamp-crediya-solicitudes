package com.co.crediya.requests.model.loanapplication.gateways;

import java.math.BigDecimal;
import reactor.core.publisher.Mono;

public interface UpdateReportService {
  Mono<String> update(long newLoansCount, BigDecimal totalAmount);
}
