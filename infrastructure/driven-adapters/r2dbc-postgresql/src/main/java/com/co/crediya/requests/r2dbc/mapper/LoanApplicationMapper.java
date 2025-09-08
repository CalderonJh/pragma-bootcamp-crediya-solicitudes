package com.co.crediya.requests.r2dbc.mapper;

import com.co.crediya.requests.model.loanapplication.LoanApplication;
import com.co.crediya.requests.model.loanapplication.LoanStatus;
import com.co.crediya.requests.model.loanapplication.LoanType;
import com.co.crediya.requests.r2dbc.entity.loan.LoanApplicationEntity;
import com.co.crediya.requests.r2dbc.projection.LoanApplicationView;

public class LoanApplicationMapper {
  private LoanApplicationMapper() {}

  public static LoanApplicationEntity toEntity(LoanApplication loanApplication) {
    return LoanApplicationEntity.builder()
        .applicantId(loanApplication.getApplicantId())
        .amount(loanApplication.getAmount())
        .termInMonths(loanApplication.getTermInMonths())
        .loanTypeId(loanApplication.getLoanType().getId())
        .loanStatusId(loanApplication.getLoanStatus().getId())
        .build();
  }

  public static LoanApplication toModel(LoanApplicationView row) {
    return LoanApplication.builder()
        .id(row.getId())
        .applicantId(row.getApplicantId())
        .amount(row.getAmount())
        .termInMonths(row.getTermInMonths())
        .loanType(
            LoanType.builder()
                .id(row.getLoanTypeId())
                .description(row.getLoanTypeDescription())
                .minAmount(row.getLoanTypeMinAmount())
                .maxAmount(row.getLoanTypeMaxAmount())
                .interestRate(row.getLoanTypeInterestRate())
                .autoValidate(row.getLoanTypeAutoValidate())
                .build())
        .loanStatus(
            LoanStatus.builder()
                .id(row.getLoanStatusId())
                .name(row.getLoanStatusName())
                .description(row.getLoanStatusDescription())
                .build())
        .build();
  }
}
