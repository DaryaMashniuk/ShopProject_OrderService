package com.innowise.orderservice.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for item")
public class ItemsRequestDto {

  @Schema(description = "Name of the item", example = "iPhone 15 Pro")
  @NotBlank(message = "Item's name can't be empty")
  @Size(max = 100, message = "Item's name can't be more than 100 symbols")
  private String name;

  @Schema(description = "Item's price", example = "999.99")
  @NotNull(message = "Item's price can't be empty")
  @DecimalMin(value = "0.01", message = "Item's price must be greater that 0")
  private BigDecimal price;
}
