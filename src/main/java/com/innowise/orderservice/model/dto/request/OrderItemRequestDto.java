package com.innowise.orderservice.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for order item")
public class OrderItemRequestDto {

  @Schema(
          description = "ID of the item to order",
          example = "1",
          requiredMode = Schema.RequiredMode.REQUIRED
  )
  @NotNull(message = "Item id is required")
  @Positive(message = "Item id must be positive")
  private Long itemId;

  @Schema(
          description = "Quantity of the item",
          example = "2",
          minimum = "1",
          maximum = "10000",
          requiredMode = Schema.RequiredMode.REQUIRED
  )
  @NotNull(message = "Quantity is required")
  @Positive(message = "Quantity must be a positive value")
  @Max(value = 10000, message = "Quantity cannot exceed 10000")
  private Integer quantity;
}