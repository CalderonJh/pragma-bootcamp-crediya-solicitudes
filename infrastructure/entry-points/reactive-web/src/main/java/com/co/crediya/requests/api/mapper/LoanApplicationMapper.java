package com.co.crediya.requests.api.mapper;

import com.co.crediya.requests.api.dto.CreateLoanApplicationDTO;
import com.co.crediya.requests.api.dto.LoanApplicantDTO;
import com.co.crediya.requests.api.dto.LoanApplicationDTO;
import com.co.crediya.requests.api.dto.LoanTypeDTO;
import com.co.crediya.requests.model.loanapplication.LoanApplication;
import com.co.crediya.requests.model.loanapplication.LoanType;
import java.util.UUID;

public class LoanApplicationMapper {

  private LoanApplicationMapper() {}

  public static LoanApplication toModel(CreateLoanApplicationDTO dto, UUID applicantId) {
    return LoanApplication.builder()
        .applicantId(applicantId)
        .amount(dto.amount())
        .termInMonths(dto.termInMonths())
        .loanType(new LoanType(dto.loanTypeId()))
        .loanStatus(null)
        .build();
  }

  public static LoanApplicationDTO toResponse(LoanApplication model, LoanApplicantDTO applicant) {
    return new LoanApplicationDTO(
        model.getId(),
        applicant,
        model.getAmount(),
        model.getTermInMonths(),
        new LoanTypeDTO(
            model.getLoanType().getDescription(), model.getLoanType().getInterestRate()),
        model.getLoanStatus().getName());
  }
}
