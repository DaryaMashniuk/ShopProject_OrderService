package com.innowise.orderservice.service;

import com.innowise.orderservice.model.Items;
import com.innowise.orderservice.model.dto.OrderItemRequestDto;
import com.innowise.orderservice.model.dto.OrderRequestDto;

import java.util.List;

public interface ItemsService {
  List<Items> getItems(List<OrderItemRequestDto> orderItemRequestDtos);
}
