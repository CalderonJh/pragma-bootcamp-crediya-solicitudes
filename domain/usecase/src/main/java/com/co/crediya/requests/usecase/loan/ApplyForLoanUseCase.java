package com.co.crediya.requests.usecase.loan;

import static com.co.crediya.requests.constant.Constant.DEFAULT_LOAN_STATUS;
import static com.co.crediya.requests.util.validation.ReactiveValidators.*;
import static reactor.core.publisher.Mono.defer;

import com.co.crediya.requests.constant.NotifyStatusType;
import com.co.crediya.requests.constant.RoleType;
import com.co.crediya.requests.exception.DataNotFoundException;
import com.co.crediya.requests.exception.InternalException;
import com.co.crediya.requests.model.util.Actor;
import com.co.crediya.requests.model.loanapplication.LoanApplication;
import com.co.crediya.requests.model.loanapplication.gateways.*;
import com.co.crediya.requests.util.validation.MessageTemplate;
import com.co.crediya.requests.util.validation.RoleValidator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ApplyForLoanUseCase {
  private final LoanApplicationRepository loanApplicationRepository;
  private final LoanStatusRepository loanStatusRepository;
  private final LoanTypeRepository loanTypeRepository;
  private final DebtCapacityService debtCapacityService;
  private final UserService userService;
  private static final Logger logger = Logger.getLogger(ApplyForLoanUseCase.class.getName());

  public Mono<Void> execute(LoanApplication loanApplication, Actor actor) {
    return Mono.just(loanApplication)
        .flatMap(app -> validateActorRole(loanApplication, actor))
        .flatMap(this::validateConstraints)
        .flatMap(this::validateReferences)
        .flatMap(this::setDefaultLoanStatus)
        .flatMap(loanApplicationRepository::saveLoanApplication)
        .flatMap(this::sendAssessDebtCapacityRequest)
        .doOnNext(la -> logger.info("Saved loan application: %s".formatted(la.toString())))
        .then();
  }

  private Mono<LoanApplication> sendAssessDebtCapacityRequest(LoanApplication loanApplication) {
    boolean automaticApproval = loanApplication.getLoanType().getAutoValidate();
    if (!automaticApproval) return Mono.just(loanApplication);

    return userService
        .getUserById(loanApplication.getApplicantId())
        .switchIfEmpty(
            Mono.error(
                new DataNotFoundException(MessageTemplate.NOT_FOUND.render("Loan applicant"))))
        .zipWith(getUserApprovedLoans(loanApplication))
        .flatMap(
            tuple ->
                debtCapacityService
                    .validateDebtCapacity(tuple.getT1(), tuple.getT2(), loanApplication)
                    .flatMap(res -> Mono.just(loanApplication)));
  }

  private Mono<List<LoanApplication>> getUserApprovedLoans(LoanApplication loanApplication) {
    return loanApplicationRepository.getByUserIdAndStatus(
        loanApplication.getApplicantId(), NotifyStatusType.APPROVED.getDbValue());
  }

  private Mono<LoanApplication> validateActorRole(LoanApplication loanApplication, Actor actor) {
    return RoleValidator.hasRole(actor, RoleType.USER).thenReturn(loanApplication);
  }

  private Mono<LoanApplication> validateConstraints(LoanApplication loanApplication) {
    return notNull(loanApplication.getApplicantId(), "Applicant id")
        .then(positive(loanApplication.getAmount(), "Loan amount"))
        .then(positive(loanApplication.getTermInMonths(), "Loan term"))
        .thenReturn(loanApplication);
  }

  private Mono<LoanApplication> validateReferences(LoanApplication loanApplication) {
    return validateExistsLoanType(loanApplication);
  }

  private Mono<LoanApplication> validateExistsLoanType(LoanApplication loanApplication) {
    UUID loanTypeId = loanApplication.getLoanType().getId();
    return notNull(loanApplication.getLoanType().getId(), "Loan type id")
        .then(
            defer(
                () ->
                    loanTypeRepository
                        .findLoanTypeById(loanTypeId)
                        .switchIfEmpty(
                            Mono.error(new DataNotFoundException("Loan type does not exist")))))
        .map(
            loanType -> {
              loanApplication.setLoanType(loanType);
              return loanApplication;
            });
  }

  private Mono<LoanApplication> setDefaultLoanStatus(LoanApplication loanApplication) {
    return loanStatusRepository
        .findLoanStatusByName(DEFAULT_LOAN_STATUS)
        .switchIfEmpty(Mono.error(new InternalException("Default loan status not found")))
        .map(
            loanStatus -> {
              loanApplication.setLoanStatus(loanStatus);
              return loanApplication;
            });
  }
}
