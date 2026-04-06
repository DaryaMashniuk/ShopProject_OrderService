package com.innowise.orderservice.kafka;

import com.innowise.orderservice.model.OrderStatus;
import com.innowise.orderservice.model.PaymentStatus;
import com.innowise.orderservice.model.events.PaymentStatusEvent;
import com.innowise.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {
  private static final Logger logger = LogManager.getLogger(PaymentEventConsumer.class);
  private final OrderService orderService;

  @KafkaListener(topics = "${kafka.payment.topic.name}")
  public void handlePaymentStatus(PaymentStatusEvent paymentStatusEvent) {
    if (paymentStatusEvent == null) {
      logger.warn("Received null PaymentEvent, skipping");
      return;
    }
    try {
      logger.info("Received Payment Event: {}" , paymentStatusEvent);
      OrderStatus newStatus = PaymentStatus.FAILED.equals(paymentStatusEvent.getPaymentStatus())
              ? OrderStatus.FAILED
              : OrderStatus.APPROVED;
      orderService.updateOrderStatus(paymentStatusEvent.getOrderId(), newStatus);
    } catch (Exception e) {
      logger.error("Failed to process PaymentEvent: {}", paymentStatusEvent, e);
    }
  }
}
