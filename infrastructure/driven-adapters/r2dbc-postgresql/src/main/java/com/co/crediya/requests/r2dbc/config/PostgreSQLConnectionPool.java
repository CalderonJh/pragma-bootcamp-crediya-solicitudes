package com.co.crediya.requests.r2dbc.config;

import static java.util.Objects.requireNonNull;

import co.com.bancolombia.secretsmanager.api.GenericManagerAsync;
import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.postgresql.client.SSLMode;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PostgreSQLConnectionPool {
    public static final int INITIAL_SIZE = 12;
    public static final int MAX_SIZE = 15;
    public static final int MAX_IDLE_TIME = 30;

  @Bean
  public ConnectionPool getConnectionConfig(
      PostgresqlConnectionProperties properties,
      GenericManagerAsync secretManager,
      @Value("${aws.secrets.rds}") String secretName)
      throws SecretException {
    RdsSecret secret = requireNonNull(secretManager.getSecret(secretName, RdsSecret.class).block());
    PostgresqlConnectionConfiguration dbConfiguration =
        PostgresqlConnectionConfiguration.builder()
            .host(secret.host())
            .port(secret.port())
            .database(properties.database())
            .schema(properties.schema())
            .username(secret.username())
            .password(secret.password())
            .sslMode(SSLMode.REQUIRE)
            .build();

        ConnectionPoolConfiguration poolConfiguration = ConnectionPoolConfiguration.builder()
                .connectionFactory(new PostgresqlConnectionFactory(dbConfiguration))
                .name("api-postgres-connection-pool")
                .initialSize(INITIAL_SIZE)
                .maxSize(MAX_SIZE)
                .maxIdleTime(Duration.ofMinutes(MAX_IDLE_TIME))
                .validationQuery("SELECT 1")
                .build();

		return new ConnectionPool(poolConfiguration);
	}
}