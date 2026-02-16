package com.innowise.orderservice.service;

import com.innowise.orderservice.model.Items;
import com.innowise.orderservice.model.OrderItems;
import com.innowise.orderservice.model.Orders;
import com.innowise.orderservice.model.dto.request.OrderItemRequestDto;

import java.util.List;

public interface OrderItemsService {

  List<OrderItems> createOrderItems(Orders order, List<OrderItemRequestDto> requestItems);
}
