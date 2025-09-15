package com.co.crediya.requests.model.loanapplication.gateways;

import com.co.crediya.requests.model.loanapplication.Applicant;
import com.co.crediya.requests.model.loanapplication.LoanApplication;
import java.util.List;
import reactor.core.publisher.Mono;

public interface DebtCapacityService {
  Mono<String> validateDebtCapacity(
      Applicant applicant, List<LoanApplication> activeLoans, LoanApplication currentApplication);
}
