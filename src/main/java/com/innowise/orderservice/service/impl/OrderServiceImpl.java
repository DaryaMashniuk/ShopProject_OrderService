package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.exceptions.ResourceNotFoundException;
import com.innowise.orderservice.model.OrderItems;
import com.innowise.orderservice.model.OrderStatus;
import com.innowise.orderservice.model.Orders;
import com.innowise.orderservice.model.dto.request.OrderItemRequestDto;
import com.innowise.orderservice.model.dto.request.OrderSearchCriteriaDto;
import com.innowise.orderservice.model.dto.request.OrderUpdateDto;
import com.innowise.orderservice.repository.OrdersRepository;
import com.innowise.orderservice.service.OrderItemsService;
import com.innowise.orderservice.service.OrderService;
import com.innowise.orderservice.specifications.OrderSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

  private final OrdersRepository ordersRepository;
  private final OrderItemsService orderItemsService;


  @Override
  public Orders createOrder(List<OrderItemRequestDto> items, Long userId) {

    Orders order = Orders.builder()
            .userId(userId)
            .status(OrderStatus.PENDING)
            .build();

    List<OrderItems> orderItems = orderItemsService.createOrderItems(order,items);
    order.setOrderItems(orderItems);
    order.setTotalPrice(calculateTotalPrice(orderItems));

    ordersRepository.save(order);

    return order;
  }

  @Override
  @Transactional(readOnly = true)
  public Orders getOrderById(Long id) {
    return ordersRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order","id",id));
  }

  @Override
  @Transactional(readOnly = true)
  public List<Orders> getOrdersByUserId(Long userId) {
    Optional<List<Orders>> orders = ordersRepository.findByUserId(userId);
    if (orders.isEmpty()) {
      throw new ResourceNotFoundException("User", "id", userId);
    }
    return orders.get();
  }

  @Override
  public Orders updateOrderById(Long id, OrderUpdateDto orderUpdateDto) {
    Orders order = ordersRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order","id",id));

    if (orderUpdateDto.getStatus() != null) {
      order.setStatus(OrderStatus.valueOf(orderUpdateDto.getStatus().toUpperCase()));
    }

    if (orderUpdateDto.getItems() != null && order.getStatus() == OrderStatus.PENDING) {
      List<OrderItems> orderItems = orderItemsService.createOrderItems(order,orderUpdateDto.getItems());

      order.getOrderItems().clear();
      order.getOrderItems().addAll(orderItems);

      order.setTotalPrice(calculateTotalPrice(orderItems));
    }

    return order;
  }

  @Override
  public void deleteOrderById(Long id) {
    Orders order = ordersRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order","id",id));
    ordersRepository.delete(order);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Orders> findAllOrders(OrderSearchCriteriaDto orderSearchCriteriaDto, Pageable pageable) {
    boolean noFilters =
            orderSearchCriteriaDto.getStatus() == null &&
                    orderSearchCriteriaDto.getFromDate() == null &&
                    orderSearchCriteriaDto.getToDate() == null;
    Page<Orders> orders;

    if (noFilters){
      orders = ordersRepository.findAll(pageable);
    } else {
      Specification<Orders> spec = OrderSpecification.build(orderSearchCriteriaDto);
      orders = ordersRepository.findAll(spec,pageable);
    }

    return orders;
  }

  private BigDecimal calculateTotalPrice(List<OrderItems> items) {
    return items
            .stream()
            .map(e -> e.getItem().getPrice().multiply(BigDecimal.valueOf(e.getQuantity())))
            .reduce(BigDecimal.valueOf(0), BigDecimal::add);
  }
}
