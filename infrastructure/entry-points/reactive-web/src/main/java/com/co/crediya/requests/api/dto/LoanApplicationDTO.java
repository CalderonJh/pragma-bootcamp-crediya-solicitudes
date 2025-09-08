package com.co.crediya.requests.api.dto;


import java.math.BigDecimal;
import java.util.UUID;

public record LoanApplicationDTO(
    UUID id,
    LoanApplicantDTO applicant,
    BigDecimal amount,
    Integer termInMonths,
    LoanTypeDTO loanType,
    String loanStatus) {}
