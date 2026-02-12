package com.innowise.orderservice.model.dto;

import com.innowise.orderservice.model.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class OrderUpdateDto {
  private List<OrderItemRequestDto> items;

  private String email;

  private OrderStatus status;
}
