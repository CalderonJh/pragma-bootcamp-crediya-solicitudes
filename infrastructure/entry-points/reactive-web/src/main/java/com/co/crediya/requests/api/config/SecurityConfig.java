package com.co.crediya.requests.api.config;

import static java.util.Objects.requireNonNull;

import co.com.bancolombia.secretsmanager.api.GenericManagerAsync;
import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.InternalException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {
  private final GenericManagerAsync secretManager;

  @Value("${aws.secrets.jwt-key-pair}")
  private String secretName;

  @Bean
  public RSAPublicKey rsaPublicKey() {
    try {
      JwtKeyPair pair = secretManager.getSecret(secretName, JwtKeyPair.class).block();
      return parsePublicKey(requireNonNull(pair).publicKey());
    } catch (SecretException e) {
      throw new InternalError(e);
    }
  }

  private RSAPublicKey parsePublicKey(String pem) {
    if (pem == null) throw new IllegalArgumentException("Public key is null");
    try {
      String sanitized =
          pem.replace("-----BEGIN PUBLIC KEY-----", "")
              .replace("-----END PUBLIC KEY-----", "")
              .replaceAll("\\s", "");
      return (RSAPublicKey)
          KeyFactory.getInstance("RSA")
              .generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(sanitized)));
    } catch (Exception e) {
      throw new InternalException("Error parsing public key", e);
    }
  }

  @Bean
  public SecurityWebFilterChain springSecurityFilterChain(
      ServerHttpSecurity http, ReactiveJwtDecoder jwtDecoder) {
    return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
        .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
        .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
        .authorizeExchange(
            ex ->
                ex.pathMatchers(
                        "/actuator/**",
                        "/health",
                        "/public/**",
                        "/webjars/swagger-ui/**",
                        "/v3/api-docs/**")
                    .permitAll()
                    .anyExchange()
                    .authenticated())
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtDecoder(jwtDecoder)))
        .build();
  }

  @Bean
  public ReactiveJwtDecoder jwtDecoder(RSAPublicKey publicKey) {
    return NimbusReactiveJwtDecoder.withPublicKey(publicKey).build();
  }
}
