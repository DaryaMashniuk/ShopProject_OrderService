package com.innowise.orderservice.service;

import com.innowise.orderservice.model.Orders;
import com.innowise.orderservice.model.dto.request.OrderItemRequestDto;
import com.innowise.orderservice.model.dto.request.OrderRequestDto;
import com.innowise.orderservice.model.dto.request.OrderSearchCriteriaDto;
import com.innowise.orderservice.model.dto.request.OrderUpdateDto;
import com.innowise.orderservice.model.dto.response.OrderResponseDto;
import com.innowise.orderservice.model.dto.response.PageResponseDto;
import com.innowise.orderservice.model.dto.response.UserOrdersListResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {
//  OrderResponseDto createOrder(OrderRequestDto orderRequestDto);
//  OrderResponseDto getOrderById(Long id);
//  UserOrdersListResponseDto getOrdersByUserId(Long userId);
//  OrderResponseDto updateOrderById(Long id, OrderUpdateDto orderUpdateDto);
//  void deleteOrderById(Long id);
//  PageResponseDto<OrderResponseDto> findAllOrders(OrderSearchCriteriaDto orderSearchCriteriaDto, Pageable pageable);
  Orders createOrder(List<OrderItemRequestDto> items, Long userId);
  Orders getOrderById(Long id);
  List<Orders> getOrdersByUserId(Long userId);
  Orders updateOrderById(Long id, OrderUpdateDto orderUpdateDto);
  void deleteOrderById(Long id);
  Page<Orders> findAllOrders(OrderSearchCriteriaDto orderSearchCriteriaDto, Pageable pageable);
}
