package com.co.crediya.requests.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateLoanApplicationDTO(BigDecimal amount, Integer termInMonths, UUID loanTypeId) {}
