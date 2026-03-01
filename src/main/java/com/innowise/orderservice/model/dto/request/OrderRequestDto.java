package com.innowise.orderservice.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for creating a new order")
public class OrderRequestDto {

  @ArraySchema(
          schema = @Schema(implementation = OrderItemRequestDto.class),
          minItems = 1,
          uniqueItems = true
  )
  @NotNull(message = "Order must have at least one item")
  @NotEmpty(message = "Order items list cannot be empty")
  @Valid
  private List<OrderItemRequestDto> items;

  @Schema(
          description = "Email of the user placing the order",
          example = "john.doe@example.com",
          requiredMode = Schema.RequiredMode.REQUIRED
  )
  @NotBlank(message = "Email is required")
  @Email(message = "Email should be valid")
  private String email;
}