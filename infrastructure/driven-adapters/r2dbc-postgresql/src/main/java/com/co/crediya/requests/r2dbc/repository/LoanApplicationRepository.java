package com.co.crediya.requests.r2dbc.repository;

import com.co.crediya.requests.r2dbc.entity.loan.LoanApplicationEntity;
import com.co.crediya.requests.r2dbc.projection.LoanApplicationView;
import java.util.UUID;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface LoanApplicationRepository
    extends ReactiveCrudRepository<LoanApplicationEntity, UUID>,
        ReactiveQueryByExampleExecutor<LoanApplicationEntity> {

	  @Query(
      """
       SELECT
             s.id_solicitud          AS id,
             s.applicant_id          AS applicant_id,
             s.monto                 AS amount,
             s.plazo                 AS term_in_months,

             t.id                    AS loan_type_id,
             t.descripcion           AS loan_type,
             t.monto_minimo          AS loan_type_min_amount,
             t.monto_maximo          AS loan_type_max_amount,
             t.tasa_interes          AS loan_type_interest_rate,
             t.validacion_automatica AS loan_type_auto_validate,

             e.id                    AS loan_status_id,
             e.nombre                AS loan_status,
             e.descripcion           AS loan_status_description
      FROM solicitudes s
               JOIN tipo_prestamo t ON s.id_tipo_prestamo = t.id
               JOIN estados e ON s.id_estado = e.id
      WHERE s.id_solicitud = :id
  """)
	Mono<LoanApplicationView> findViewById(UUID id);
}
