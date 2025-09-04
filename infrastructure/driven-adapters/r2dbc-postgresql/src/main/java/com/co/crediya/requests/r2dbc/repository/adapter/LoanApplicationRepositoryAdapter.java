package com.co.crediya.requests.r2dbc.repository.adapter;

import com.co.crediya.requests.model.loanapplication.LoanApplication;
import com.co.crediya.requests.model.loanapplication.LoanApplicationFilter;
import com.co.crediya.requests.model.util.pagination.Page;
import com.co.crediya.requests.model.util.pagination.Pageable;
import com.co.crediya.requests.model.util.pagination.Sort;
import com.co.crediya.requests.r2dbc.entity.LoanApplicationEntity;
import com.co.crediya.requests.r2dbc.helper.ReactiveAdapterOperations;
import com.co.crediya.requests.r2dbc.mapper.LoanApplicationMapper;
import com.co.crediya.requests.r2dbc.projection.LoanApplicationView;
import com.co.crediya.requests.r2dbc.repository.LoanApplicationRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class LoanApplicationRepositoryAdapter
    extends ReactiveAdapterOperations<
        LoanApplication, LoanApplicationEntity, UUID, LoanApplicationRepository>
    implements com.co.crediya.requests.model.loanapplication.gateways.LoanApplicationRepository {
  private final R2dbcEntityTemplate template;

  public LoanApplicationRepositoryAdapter(
      LoanApplicationRepository repository, ObjectMapper mapper, R2dbcEntityTemplate template) {
    super(repository, mapper, d -> mapper.map(d, LoanApplication.class));
    this.template = template;
  }

  @Override
  public Mono<LoanApplication> saveLoanApplication(LoanApplication loanApplication) {
    LoanApplicationEntity entity = LoanApplicationMapper.toEntity(loanApplication);
    return repository.save(entity).map(LoanApplicationMapper::toModel);
  }

  @Override
  public Mono<Page<LoanApplication>> getLoanApplicationsPage(
      Pageable pageable, LoanApplicationFilter filter) {
    String orderByExpression = buildOrderByExpression(pageable.getSort());
    final String LOAN_APL_PAGE_BASE_QUERY =
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
		""";

    StringBuilder sqlBuilder = new StringBuilder(LOAN_APL_PAGE_BASE_QUERY);

    if (filter.byStatus()) sqlBuilder.append(" WHERE s.id_estado = :statusId ");

    sqlBuilder
        .append(" ORDER BY ")
        .append(orderByExpression)
        .append(" LIMIT :limit OFFSET :offset");

    String sql = sqlBuilder.toString();

    var spec =
        template
            .getDatabaseClient()
            .sql(sql)
            .bind("limit", pageable.getPageSize())
            .bind("offset", pageable.getOffset());

    if (filter.byStatus()) spec = spec.bind("statusId", filter.loanStatusId());

    Mono<List<LoanApplication>> content =
        spec.map(
                (row, metadata) ->
                    template.getConverter().read(LoanApplicationView.class, row, metadata))
            .all()
            .map(LoanApplicationMapper::toModel)
            .collectList();

    Mono<Long> total = countAll(filter);

    return Mono.zip(content, total)
        .map(tuple -> new Page<>(tuple.getT1(), pageable, tuple.getT2()));
  }

  private String buildOrderByExpression(Sort sort) {
    final String defaultOrder = "id";
    if (sort == null || sort.isUnsorted()) return defaultOrder;

    final Map<String, String> orderParams =
        Map.ofEntries(
            Map.entry("amount", "amount"),
            Map.entry("applicant", "applicant_id"),
            Map.entry("termInMonths", "term_in_months"),
            Map.entry("loanType.description", "loan_type"),
            Map.entry("loanType.interestRate", "loan_type_interest_rate"),
            Map.entry("loanStatus", "loan_status_description"));

    List<String> parts = new ArrayList<>();
    for (Sort.Order o : sort.getOrders()) {
      String expr = orderParams.get(o.getProperty());
      if (expr != null) {
        if (o.isDescending()) expr = expr + " DESC";
        parts.add(expr);
      }
    }
    return parts.isEmpty() ? defaultOrder : String.join(", ", parts);
  }

  private Mono<Long> countAll(LoanApplicationFilter filter) {
    String countSql = "SELECT COUNT(*) FROM solicitudes s";
    if (filter.byStatus()) countSql += " WHERE s.id_estado = :statusId";

    var countSpec = template.getDatabaseClient().sql(countSql);
    if (filter.byStatus()) countSpec = countSpec.bind("statusId", filter.loanStatusId());

    return countSpec.map((row, md) -> row.get(0, Long.class)).one();
  }

  @Override
  public Flux<LoanApplication> getLoanApplications() {
    return repository.findAllView().map(LoanApplicationMapper::toModel);
  }
}
