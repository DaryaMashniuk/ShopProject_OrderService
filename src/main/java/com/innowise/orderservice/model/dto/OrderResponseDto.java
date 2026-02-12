package com.innowise.orderservice.model.dto;

import com.innowise.orderservice.model.Orders;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class OrderResponseDto {

  private Orders order;
}
