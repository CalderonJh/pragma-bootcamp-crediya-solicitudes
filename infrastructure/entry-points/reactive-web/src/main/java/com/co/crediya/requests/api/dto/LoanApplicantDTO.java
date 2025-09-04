package com.co.crediya.requests.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record LoanApplicantDTO(UUID id, String email, String name, String lastName, BigDecimal baseSalary) {}
