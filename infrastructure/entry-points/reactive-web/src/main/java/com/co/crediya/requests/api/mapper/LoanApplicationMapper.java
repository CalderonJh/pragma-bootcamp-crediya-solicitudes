package com.co.crediya.requests.api.mapper;

import com.co.crediya.requests.api.dto.LoanApplicationDTO;
import com.co.crediya.requests.model.loanapplication.LoanApplication;
import com.co.crediya.requests.model.loanapplication.LoanType;

public class LoanApplicationMapper {

  private LoanApplicationMapper() {}

  public static LoanApplication toModel(LoanApplicationDTO dto, String email) {
    return LoanApplication.builder()
        .applicantEmail(email)
        .amount(dto.getAmount())
        .termInMonths(dto.getTermInMonths())
        .loanType(new LoanType(dto.getLoanTypeId()))
        .loanStatus(null)
        .build();
  }
}
