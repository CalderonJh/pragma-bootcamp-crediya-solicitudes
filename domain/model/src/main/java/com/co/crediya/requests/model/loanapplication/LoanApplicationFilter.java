package com.co.crediya.requests.model.loanapplication;

import java.util.UUID;

public record LoanApplicationFilter(UUID loanStatusId) {
	public boolean byStatus() {
		return loanStatusId != null;
	}
}
