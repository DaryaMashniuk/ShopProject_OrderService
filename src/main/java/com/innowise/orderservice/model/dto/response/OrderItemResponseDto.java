package com.innowise.orderservice.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for order item")
public class OrderItemResponseDto {

  @Schema(description = "Order item ID", example = "1")
  private Long id;

  @Schema(description = "Item ID", example = "5")
  private Long itemId;

  @Schema(description = "Item name", example = "Laptop")
  private String itemName;

  @Schema(description = "Item price per unit", example = "999.99")
  private BigDecimal itemPrice;

  @Schema(description = "Quantity ordered", example = "2")
  private Integer quantity;

}