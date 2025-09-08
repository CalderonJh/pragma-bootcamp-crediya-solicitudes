package com.co.crediya.requests.r2dbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.co.crediya.requests.model.loanapplication.LoanApplication;
import com.co.crediya.requests.model.loanapplication.LoanStatus;
import com.co.crediya.requests.model.loanapplication.LoanType;
import com.co.crediya.requests.r2dbc.entity.loan.LoanApplicationEntity;
import com.co.crediya.requests.r2dbc.mapper.LoanApplicationMapper;
import com.co.crediya.requests.r2dbc.projection.LoanApplicationView;
import com.co.crediya.requests.r2dbc.repository.LoanApplicationRepository;
import com.co.crediya.requests.r2dbc.repository.adapter.LoanApplicationRepositoryAdapter;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class LoanApplicationRepositoryAdapterTest {

  @InjectMocks LoanApplicationRepositoryAdapter repositoryAdapter;

  @Mock LoanApplicationRepository repository;

  @Test
  void mustSaveLoanApplication() {
    LoanApplication loan =
        LoanApplication.builder()
            .id(UUID.randomUUID())
            .applicantId(UUID.randomUUID())
            .loanType(new LoanType(UUID.randomUUID()))
            .loanStatus(new LoanStatus(UUID.randomUUID(), "PENDING", "Pending"))
            .build();
    LoanApplicationEntity entity = LoanApplicationMapper.toEntity(loan);

    LoanApplicationView view = new LoanApplicationView();
    view.setApplicantId(entity.getApplicantId());
    view.setLoanTypeId(entity.getLoanTypeId());
    view.setLoanStatusId(entity.getLoanStatusId());
		
    when(repository.save(any(LoanApplicationEntity.class))).thenReturn(Mono.just(entity));
    when(repository.findViewById(any())).thenReturn(Mono.just(view));

    Mono<LoanApplication> result = repositoryAdapter.saveLoanApplication(loan);

    StepVerifier.create(result)
        .assertNext(
            la -> {
              assertThat(la.getApplicantId()).isEqualTo(loan.getApplicantId());
              assertThat(la.getLoanType().getId()).isNotNull();
              assertThat(la.getLoanStatus().getId()).isEqualTo(loan.getLoanStatus().getId());
            })
        .verifyComplete();
  }
}
