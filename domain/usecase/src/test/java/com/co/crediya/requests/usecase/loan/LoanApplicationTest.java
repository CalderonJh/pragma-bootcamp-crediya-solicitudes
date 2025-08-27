package com.co.crediya.requests.usecase.loan;

import static com.co.crediya.requests.util.validation.ReactiveValidators.MessageTemplate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.co.crediya.requests.exception.BusinessRuleException;
import com.co.crediya.requests.exception.DataNotFoundException;
import com.co.crediya.requests.exception.InternalException;
import com.co.crediya.requests.model.loanapplication.LoanApplication;
import com.co.crediya.requests.model.loanapplication.LoanType;
import com.co.crediya.requests.model.loanapplication.gateways.LoanApplicationRepository;
import com.co.crediya.requests.model.loanapplication.gateways.LoanStatusRepository;
import com.co.crediya.requests.model.loanapplication.gateways.LoanTypeRepository;
import com.co.crediya.requests.util.Constant;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class LoanApplicationTest {
  private LoanApplicationRepository loanApplicationRepository;
  private LoanStatusRepository loanStatusRepository;
  private LoanTypeRepository loanTypeRepository;
  private LoanApplicationUseCase useCase;
  private LoanApplication loanApplication;

  @BeforeEach
  void setUp() {
    loanApplicationRepository = mock(LoanApplicationRepository.class);
    loanStatusRepository = mock(LoanStatusRepository.class);
    loanTypeRepository = mock(LoanTypeRepository.class);
    useCase =
        new LoanApplicationUseCase(
            loanApplicationRepository, loanStatusRepository, loanTypeRepository);

    loanApplication =
        LoanApplication.builder()
            .id(UUID.randomUUID())
            .applicantEmail("email@email.com")
            .amount(BigDecimal.valueOf(1000000))
            .termInMonths(12)
            .loanType(new LoanType(UUID.randomUUID()))
            .loanStatus(null)
            .build();
  }

  @Test
  @DisplayName("Get all loan application calls repository method")
  void getAllLoanApplications() {
    doReturn(Flux.empty()).when(loanApplicationRepository).getLoanApplications();
    Flux<LoanApplication> result = useCase.getAllLoanApplications();
    StepVerifier.create(result).expectNextCount(0).verifyComplete();
    verify(loanApplicationRepository).getLoanApplications();
  }

  @Test
  @DisplayName("Error when email is invalid")
  void errorWhenEmailIsInvalid() {
    loanApplication.setApplicantEmail("invalid");
    StepVerifier.create(useCase.applyForLoan(loanApplication))
        .expectErrorSatisfies(
            error ->
                assertThat(error)
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage(MessageTemplate.EMAIL.render()))
        .verify();
  }

  @Test
  @DisplayName("Error when loan type amount is zero")
  void errorWhenLoanAmountIsInvalid() {
    loanApplication.setAmount(BigDecimal.valueOf(0));

    StepVerifier.create(useCase.applyForLoan(loanApplication))
        .expectErrorSatisfies(
            e ->
                assertThat(e)
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage(MessageTemplate.POSITIVE.render("Loan type")))
        .verify();
  }

  @Test
  @DisplayName("Error when loan term is negative")
  void errorWhenLoanTermIsInvalid() {
    loanApplication.setTermInMonths(-1);
    StepVerifier.create(useCase.applyForLoan(loanApplication))
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
    StepVerifier.create(useCase.applyForLoan(loanApplication))
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
    StepVerifier.create(useCase.applyForLoan(loanApplication))
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

    StepVerifier.create(useCase.applyForLoan(loanApplication)).verifyComplete();

    verify(loanTypeRepository).findLoanTypeById(loanApplication.getLoanType().getId());
    verify(loanStatusRepository).findLoanStatusByName(Constant.DEFAULT_LOAN_STATUS);
    verify(loanApplicationRepository).saveLoanApplication(loanApplication);
  }
}
