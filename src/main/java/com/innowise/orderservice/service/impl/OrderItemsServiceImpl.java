package com.innowise.orderservice.service.impl;
import com.innowise.orderservice.exceptions.QuantityIsNotRegisteredException;
import com.innowise.orderservice.model.Items;
import com.innowise.orderservice.model.OrderItems;
import com.innowise.orderservice.model.Orders;
import com.innowise.orderservice.model.dto.OrderItemRequestDto;
import com.innowise.orderservice.repository.OrderItemsRepository;
import com.innowise.orderservice.service.OrderItemsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional
@Service
public class OrderItemsServiceImpl implements OrderItemsService {

  private final OrderItemsRepository orderItemsRepository;

  @Override
  public List<OrderItems> createOrderItems(List<Items> items, Orders order, List<OrderItemRequestDto> requestItems) {

    return items.stream()
            .map(item -> {

              int quantity = requestItems
                      .stream()
                      .filter(e -> Objects.equals(e.getItemId(), item.getId()))
                      .map(OrderItemRequestDto::getQuantity)
                      .findFirst()
                      .orElseThrow(() -> new QuantityIsNotRegisteredException("No quantity for item : "+item.getId()));

              return OrderItems.builder()
                            .order(order)
                            .item(item)
                            .quantity(quantity)
                            .build();
            }
            ).toList();
  }

  @Override
  public void deleteOrderItemsForOrderById(long orderId){
    orderItemsRepository.deleteByOrderId(orderId);
  }
}
