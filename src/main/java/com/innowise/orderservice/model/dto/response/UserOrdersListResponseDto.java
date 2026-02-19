package com.innowise.orderservice.model.dto.response;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for list of user orders")
public class UserOrdersListResponseDto {

  @Schema(description = "List of orders")
  private List<OrderResponseFromListDto> orders;

  @Schema(description = "User information")
  private UserResponseDto user;

}
