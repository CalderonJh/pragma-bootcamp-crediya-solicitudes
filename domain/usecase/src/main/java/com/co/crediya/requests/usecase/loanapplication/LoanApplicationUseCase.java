package com.co.crediya.requests.usecase.loanapplication;

import static com.co.crediya.requests.usecase.util.ValidationUtils.isValidEmail;

import com.co.crediya.requests.model.loanapplication.LoanApplication;
import com.co.crediya.requests.model.loanapplication.gateways.LoanApplicationRepository;
import com.co.crediya.requests.model.loanapplication.gateways.LoanStatusRepository;
import com.co.crediya.requests.model.loanapplication.gateways.LoanTypeRepository;
import com.co.crediya.requests.usecase.exception.BusinessRuleException;
import com.co.crediya.requests.usecase.exception.DataNotFoundException;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class LoanApplicationUseCase {
  private final LoanApplicationRepository loanApplicationRepository;
  private final LoanStatusRepository loanStatusRepository;
  private final LoanTypeRepository loanTypeRepository;

  public Mono<Void> applyForLoan(LoanApplication loanApplication) {
    return Mono.just(loanApplication)
        .flatMap(this::validateConstraints)
        .flatMap(this::setDefaultLoanStatus)
        .flatMap(loanApplicationRepository::saveLoanApplication)
        .then();
  }

  Mono<LoanApplication> validateConstraints(LoanApplication loanApplication) {
    if (!isValidEmail(loanApplication.getApplicantEmail()))
      return Mono.error(new BusinessRuleException("Invalid email format"));
    if (loanApplication.getAmount().compareTo(BigDecimal.ZERO) <= 0)
      return Mono.error(new BusinessRuleException("Loan amount must be greater than zero"));
    if (loanApplication.getTermInMonths() <= 0)
      return Mono.error(new BusinessRuleException("Loan term must be greater than zero"));
    return validateExistsLoanType(loanApplication);
  }

  private Mono<LoanApplication> validateExistsLoanType(LoanApplication loanApplication) {
    UUID loanTypeId = loanApplication.getLoanType().getId();
    return loanTypeRepository
        .findLoanTypeById(loanTypeId)
        .switchIfEmpty(Mono.error(new BusinessRuleException("Loan type does not exist")))
        .map(
            loanType -> {
              loanApplication.setLoanType(loanType);
              return loanApplication;
            });
  }

  Mono<LoanApplication> setDefaultLoanStatus(LoanApplication loanApplication) {
    return loanStatusRepository
        .findLoanStatusByName("PENDIENTE")
        .switchIfEmpty(
            Mono.error(new DataNotFoundException("Default loan status 'PENDIENTE' not found")))
        .map(
            loanStatus -> {
              loanApplication.setLoanStatus(loanStatus);
              return loanApplication;
            });
  }
}
