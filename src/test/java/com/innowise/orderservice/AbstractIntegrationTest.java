package com.innowise.orderservice;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;


@Testcontainers
@EmbeddedKafka(partitions = 1, topics = {"order-created-topic", "payment-events-topic"})
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

    registry.add("spring.kafka.bootstrap-servers", () -> "localhost:9092");
    registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
    registry.add("spring.kafka.consumer.group-id", () -> "test-group");
    registry.add("kafka.order.topic.name", () -> "order-created-topic");
    registry.add("kafka.payment.topic.name", () -> "payment-events-topic");
  }

  protected abstract WireMockExtension getWireMockExtension();
}


