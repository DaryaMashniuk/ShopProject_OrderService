package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.exceptions.UserDoesNotExistException;
import com.innowise.orderservice.exceptions.UserOrdersDoNotExistException;
import com.innowise.orderservice.model.Items;
import com.innowise.orderservice.model.OrderItems;
import com.innowise.orderservice.model.OrderStatus;
import com.innowise.orderservice.model.Orders;
import com.innowise.orderservice.model.dto.OrderRequestDto;
import com.innowise.orderservice.model.dto.OrderSearchCriteriaDto;
import com.innowise.orderservice.model.dto.OrderUpdateDto;
import com.innowise.orderservice.repository.OrdersRepository;
import com.innowise.orderservice.service.ItemsService;
import com.innowise.orderservice.service.OrderItemsService;
import com.innowise.orderservice.service.OrderService;
import com.innowise.orderservice.specifications.OrderSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.print.Pageable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

  private final ItemsService itemsService;
  private final OrdersRepository ordersRepository;
  private final OrderItemsService orderItemsService;

  //TODO return response object

  public Orders createOrder(OrderRequestDto orderRequestDto) {

    List<Items> items = itemsService.getItems(orderRequestDto.getItems());
    //TODO add userId
    Orders order = Orders.builder()
            .status(OrderStatus.NEW)
            .deleted(false)
            .build();

    List<OrderItems> orderItems = orderItemsService.createOrderItems(items,order,orderRequestDto.getItems());
    order.setTotalPrice(calculateTotalPrice(orderItems));
    order.setOrderItems(orderItems);
    ordersRepository.save(order);

    return order;
  }

  @Transactional(readOnly = true)
  public Orders getOrderById(Long id) {

    return ordersRepository.findById(id)
            .orElseThrow(() -> new UserDoesNotExistException("User does not exist with id "+id));
  }

  @Transactional(readOnly = true)
  public List<Orders> getOrdersByUserId(Long userId) {
    Optional<List<Orders>> orders = ordersRepository.findByUserId(userId);
    if (orders.isEmpty()) {
      throw new UserOrdersDoNotExistException("No orders for user "+userId);
    }
    return orders.get();
  }

  public Orders updateOrderById(Long id, OrderUpdateDto orderUpdateDto) {
    Orders order = ordersRepository.findById(id)
            .orElseThrow(() -> new UserDoesNotExistException("User does not exist with id "+id));

    if (orderUpdateDto.getStatus() != null) {
      order.setStatus(orderUpdateDto.getStatus());
    }

    if (orderUpdateDto.getItems() != null && orderUpdateDto.getStatus() == OrderStatus.PENDING) {
      List<Items> items = itemsService.getItems(orderUpdateDto.getItems());
      orderItemsService.deleteOrderItemsForOrderById(order.getId());
      List<OrderItems> orderItems = orderItemsService.createOrderItems(items,order,orderUpdateDto.getItems());

      order.setTotalPrice(calculateTotalPrice(orderItems));
      order.setOrderItems(orderItems);
    }
    return order;
  }

  public void deleteOrderById(Long id) {
    Orders order = ordersRepository.findById(id)
            .orElseThrow(() -> new UserDoesNotExistException("User does not exist with id "+id));
    ordersRepository.delete(order);
  }

  public Page<Orders> findAllOrders(OrderSearchCriteriaDto orderSearchCriteriaDto, Pageable pageable) {
    boolean noFilters =
            orderSearchCriteriaDto.getStatus() == null &&
                    (orderSearchCriteriaDto.getFromDate() == null
                    || orderSearchCriteriaDto.getToDate() == null);
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
