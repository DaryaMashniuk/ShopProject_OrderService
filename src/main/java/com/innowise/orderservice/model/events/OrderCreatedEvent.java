package com.innowise.orderservice.model.events;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreatedEvent {
  @NotNull
  @Schema(description = "Order ID", example = "1")
  private Long orderId;

  @NotNull
  @Positive
  @Schema(description = "Total order price", example = "1999.98")
  private BigDecimal totalPrice;

}
