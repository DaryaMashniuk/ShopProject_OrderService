package com.innowise.orderservice.kafka;

import com.innowise.orderservice.model.events.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class OrderEventProducer {
  @Value("${kafka.order.topic.name}")
  private String topicName;

  private static final Logger logger = LogManager.getLogger(OrderEventProducer.class);
  private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

  public void sendOrderCreatedEvent(Long orderId, BigDecimal amount) {
    OrderCreatedEvent event = new OrderCreatedEvent(orderId, amount);
    kafkaTemplate.send(topicName, String.valueOf(event.getOrderId()),event)
            .whenComplete((message, exception) -> {
              if (exception != null) {
                logger.error("Failed to send order created event for Order ID {}. Reason: {}", event.getOrderId(), exception.getMessage());
              } else {
                logger.info("Successfully sent order created info for order {} ", event.getOrderId());
              }
            });
  }


}
