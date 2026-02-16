package com.innowise.orderservice.model.dto.request;

import com.innowise.orderservice.model.OrderStatus;
import com.innowise.orderservice.validator.ValidEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for updating an order")
public class OrderUpdateDto {

  @ArraySchema(schema = @Schema(implementation = OrderItemRequestDto.class))
  @Valid
  private List<OrderItemRequestDto> items;

  @Schema(
          description = "Email of the user",
          example = "john.doe@example.com"
  )
  @NotBlank(message = "Email is required")
  @Email(message = "Email should be valid")
  private String email;

  @Schema(
          description = "Order status",
          example = "APPROVED",
          allowableValues = {"PENDING", "APPROVED", "DELIVERED"}
  )
  @ValidEnum(enumClass = OrderStatus.class, message = "Invalid order status")
  private String status;
}