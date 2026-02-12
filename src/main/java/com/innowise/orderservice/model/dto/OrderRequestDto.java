package com.innowise.orderservice.model.dto;

import com.innowise.orderservice.model.Items;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class OrderRequestDto {

  private List<OrderItemRequestDto> items;

  private String email;


}
