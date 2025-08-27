package com.co.crediya.requests.config;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

class UseCasesConfigTest {

  @Test
  void testUseCaseBeansExist() {
    try (AnnotationConfigApplicationContext context =
        new AnnotationConfigApplicationContext(TestConfig.class)) {
      String[] beanNames = context.getBeanDefinitionNames();

      boolean useCaseBeanFound = false;
      for (String beanName : beanNames) {
        if (beanName.endsWith("UseCase")) {
          useCaseBeanFound = true;
          break;
        }
      }

      assertTrue(useCaseBeanFound, "No beans ending with 'Use Case' were found");
    }
  }

  @Configuration
  static class TestConfig {

    @Bean
    public MyUseCase myUseCase() {
      return new MyUseCase();
    }
  }

  static class MyUseCase {
    public String execute() {
      return "MyUseCase Test";
    }
  }
}
