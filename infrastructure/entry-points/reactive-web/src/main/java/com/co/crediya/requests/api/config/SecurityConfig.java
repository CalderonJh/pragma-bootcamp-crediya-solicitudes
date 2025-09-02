package com.co.crediya.requests.api.config;

import java.io.IOException;
import java.io.InputStream;
import java.security.interfaces.RSAPublicKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
  @Bean
  public RSAPublicKey rsaPublicKey(@Value("${security.jwt.public-key-path}") Resource publicKey)
      throws IOException {
    try (InputStream inputStream = publicKey.getInputStream()) {
      return RsaKeyConverters.x509().convert(inputStream);
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
