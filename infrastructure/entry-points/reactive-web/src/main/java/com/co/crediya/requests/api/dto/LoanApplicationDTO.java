package com.co.crediya.requests.api.dto;


import java.math.BigDecimal;

public record LoanApplicationDTO(
    LoanApplicantDTO applicant, BigDecimal amount, Integer termInMonths, LoanTypeDTO loanType, String loanStatus) {}
