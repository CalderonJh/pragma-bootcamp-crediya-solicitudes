package com.co.crediya.requests.r2dbc.mapper;

import com.co.crediya.requests.model.loanapplication.LoanApplication;
import com.co.crediya.requests.r2dbc.entity.LoanApplicationEntity;

public class LoanApplicationMapper {
  private LoanApplicationMapper() {}

  public static LoanApplicationEntity toEntity(LoanApplication loanApplication) {
    return LoanApplicationEntity.builder()
        .applicantEmail(loanApplication.getApplicantEmail())
        .amount(loanApplication.getAmount())
        .termInMonths(loanApplication.getTermInMonths())
        .loanTypeId(loanApplication.getLoanType().getId())
        .loanStatusId(loanApplication.getLoanStatus().getId())
        .build();
  }
}
