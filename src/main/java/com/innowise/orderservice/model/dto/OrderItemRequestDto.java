package com.innowise.orderservice.model.dto;

import lombok.Data;

@Data
public class OrderItemRequestDto {

  private Long itemId;

  private int quantity;
}
