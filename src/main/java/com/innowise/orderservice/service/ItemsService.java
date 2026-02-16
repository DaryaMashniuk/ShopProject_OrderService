package com.innowise.orderservice.service;

import com.innowise.orderservice.model.Items;
import com.innowise.orderservice.model.dto.request.OrderItemRequestDto;

import java.util.List;

public interface ItemsService {
  List<Items> getItems(List<OrderItemRequestDto> orderItemRequestDtos);
}
