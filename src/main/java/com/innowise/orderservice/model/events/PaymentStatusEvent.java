package com.innowise.orderservice.model.events;

import com.innowise.orderservice.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentStatusEvent {

  private Long orderId;
  private PaymentStatus paymentStatus;

}
