package com.innowise.orderservice.model.dto.response;

import com.innowise.orderservice.model.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for order")
public class OrderResponseDto {

  @Schema(description = "Order ID", example = "1")
  private Long id;

  @Schema(description = "User information")
  private UserResponseDto user;

  @Schema(description = "Total order price", example = "1999.98")
  private BigDecimal totalPrice;

  @Schema(
          description = "Order status",
          example = "PENDING",
          allowableValues = {"PENDING", "APPROVED", "DELIVERED"}
  )
  private OrderStatus status;

  @Schema(description = "List of ordered items")
  private List<OrderItemResponseDto> orderItems;

  @Schema(description = "Order creation timestamp", example = "2024-01-15T10:30:00")
  private LocalDateTime createdAt;

  @Schema(description = "Order last update timestamp", example = "2024-01-15T10:30:00")
  private LocalDateTime updatedAt;
}