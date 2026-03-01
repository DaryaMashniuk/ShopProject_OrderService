package com.innowise.orderservice.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for item")
public class ItemsResponseDto {

  @Schema(description = "Item ID", example = "1")
  private Long id;

  @Schema(description = "Item's name", example = "iPhone 15 Pro")
  private String name;

  @Schema(description = "Item's price", example = "999.99")
  private BigDecimal price;

  @Schema(description = "Item creation timestamp", example = "2024-01-15T10:30:00")
  private LocalDateTime createdAt;

  @Schema(description = "Item last update timestamp", example = "2024-01-15T10:30:00")
  private LocalDateTime updatedAt;
}