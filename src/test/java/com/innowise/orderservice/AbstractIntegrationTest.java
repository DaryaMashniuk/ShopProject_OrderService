package com.innowise.orderservice;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.innowise.orderservice.controller.OrderControllerTest.wireMockExtension;

@Testcontainers
public abstract class AbstractIntegrationTest {

  static final PostgreSQLContainer<?> POSTGRES =
          new PostgreSQLContainer<>("postgres:15-alpine")
                  .withDatabaseName("testdb")
                  .withUsername("test")
                  .withPassword("test");


  static {
    POSTGRES.start();
  }

  @DynamicPropertySource
  static void registerProps(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
    registry.add("userservice.url", () -> "http://localhost:8081");
  }

  protected abstract WireMockExtension getWireMockExtension();
}


