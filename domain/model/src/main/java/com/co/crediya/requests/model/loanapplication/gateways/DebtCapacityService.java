package com.co.crediya.requests.model.loanapplication.gateways;

import com.co.crediya.requests.model.loanapplication.User;
import com.co.crediya.requests.model.loanapplication.LoanApplication;
import java.util.List;
import reactor.core.publisher.Mono;

public interface DebtCapacityService {
  Mono<String> validateDebtCapacity(
		User user, List<LoanApplication> activeLoans, LoanApplication currentApplication);
}
