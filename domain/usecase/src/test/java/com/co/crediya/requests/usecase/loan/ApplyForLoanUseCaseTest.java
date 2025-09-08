package com.co.crediya.requests.usecase.loan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.co.crediya.requests.constant.RoleType;
import com.co.crediya.requests.exception.BusinessRuleException;
import com.co.crediya.requests.exception.DataNotFoundException;
import com.co.crediya.requests.exception.InternalException;
import com.co.crediya.requests.exception.PermissionException;
import com.co.crediya.requests.model.loanapplication.Actor;
import com.co.crediya.requests.model.loanapplication.LoanApplication;
import com.co.crediya.requests.model.loanapplication.LoanType;
import com.co.crediya.requests.model.loanapplication.gateways.LoanApplicationRepository;
import com.co.crediya.requests.model.loanapplication.gateways.LoanStatusRepository;
import com.co.crediya.requests.model.loanapplication.gateways.LoanTypeRepository;
import com.co.crediya.requests.constant.Constant;
import java.math.BigDecimal;
import java.util.UUID;

import com.co.crediya.requests.util.validation.MessageTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class ApplyForLoanUseCaseTest {
  private LoanApplicationRepository loanApplicationRepository;
  private LoanStatusRepository loanStatusRepository;
  private LoanTypeRepository loanTypeRepository;
  private ApplyForLoanUseCase useCase;
  private LoanApplication loanApplication;
  private Actor actor;

  @BeforeEach
  void setUp() {
    loanApplicationRepository = mock(LoanApplicationRepository.class);
    loanStatusRepository = mock(LoanStatusRepository.class);
    loanTypeRepository = mock(LoanTypeRepository.class);
    useCase =
        new ApplyForLoanUseCase(
            loanApplicationRepository, loanStatusRepository, loanTypeRepository);

    loanApplication =
        LoanApplication.builder()
            .id(UUID.randomUUID())
            .applicantId(UUID.randomUUID())
            .amount(BigDecimal.valueOf(1000000))
            .termInMonths(12)
            .loanType(new LoanType(UUID.randomUUID()))
            .loanStatus(null)
            .build();
    actor = new Actor(UUID.randomUUID(), RoleType.USER.getValue());
  }

  @Test
  @DisplayName("Error if actor role is not USER")
  void errorIfActorRoleIsNotUser() {
    actor.setRole("OTHER");
    StepVerifier.create(useCase.execute(loanApplication, actor))
        .expectErrorSatisfies(
            error ->
                assertThat(error)
                    .isInstanceOf(PermissionException.class)
                    .hasMessage(MessageTemplate.NOT_AUTHORIZED.render()))
        .verify();
  }

  @Test
  @DisplayName("Error when loan amount is zero")
  void errorWhenLoanAmountIsInvalid() {
    loanApplication.setAmount(BigDecimal.valueOf(0));

    StepVerifier.create(useCase.execute(loanApplication, actor))
        .expectErrorSatisfies(
            e ->
                assertThat(e)
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage(MessageTemplate.POSITIVE.render("Loan amount")))
        .verify();
  }

  @Test
  @DisplayName("Error when loan term is negative")
  void errorWhenLoanTermIsInvalid() {
    loanApplication.setTermInMonths(-1);
    StepVerifier.create(useCase.execute(loanApplication, actor))
        .expectErrorSatisfies(
            e ->
                assertThat(e)
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage(MessageTemplate.POSITIVE.render("Loan term")))
        .verify();
  }

  @Test
  @DisplayName("Error when loan type does not exist")
  void errorWhenLoanTypeDoesNotExist() {
    when(loanTypeRepository.findLoanTypeById(loanApplication.getLoanType().getId()))
        .thenReturn(Mono.empty());
    StepVerifier.create(useCase.execute(loanApplication, actor))
        .expectErrorSatisfies(
            e ->
                assertThat(e)
                    .isInstanceOf(DataNotFoundException.class)
                    .hasMessage("Loan type does not exist"))
        .verify();
  }

  @Test
  @DisplayName("Error when default loan status does not exist")
  void errorWhenDefaultLoanStatusDoesNotExist() {
    when(loanTypeRepository.findLoanTypeById(loanApplication.getLoanType().getId()))
        .thenReturn(Mono.just(new LoanType(loanApplication.getLoanType().getId())));
    when(loanStatusRepository.findLoanStatusByName(Constant.DEFAULT_LOAN_STATUS))
        .thenReturn(Mono.empty());
    StepVerifier.create(useCase.execute(loanApplication, actor))
        .expectErrorSatisfies(
            e ->
                assertThat(e)
                    .isInstanceOf(InternalException.class)
                    .hasMessage("Default loan status not found"))
        .verify();
  }

  @Test
  @DisplayName("Successful loan application")
  void successfulLoanApplication() {
    when(loanTypeRepository.findLoanTypeById(loanApplication.getLoanType().getId()))
        .thenReturn(Mono.just(new LoanType(loanApplication.getLoanType().getId())));
    when(loanStatusRepository.findLoanStatusByName(Constant.DEFAULT_LOAN_STATUS))
        .thenReturn(
            Mono.just(
                new com.co.crediya.requests.model.loanapplication.LoanStatus(
                    UUID.randomUUID(), Constant.DEFAULT_LOAN_STATUS, "")));
    when(loanApplicationRepository.saveLoanApplication(loanApplication)).thenReturn(Mono.empty());

    StepVerifier.create(useCase.execute(loanApplication, actor)).verifyComplete();

    verify(loanTypeRepository).findLoanTypeById(loanApplication.getLoanType().getId());
    verify(loanStatusRepository).findLoanStatusByName(Constant.DEFAULT_LOAN_STATUS);
    verify(loanApplicationRepository).saveLoanApplication(loanApplication);
  }
}
