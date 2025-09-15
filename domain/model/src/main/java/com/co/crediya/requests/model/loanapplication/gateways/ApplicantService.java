package com.co.crediya.requests.model.loanapplication.gateways;

import com.co.crediya.requests.model.loanapplication.Applicant;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ApplicantService {
  Mono<Applicant> getApplicantById(UUID applicantId);
}
