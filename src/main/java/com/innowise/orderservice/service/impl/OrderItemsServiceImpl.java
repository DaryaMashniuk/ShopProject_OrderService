package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.model.Items;
import com.innowise.orderservice.model.OrderItems;
import com.innowise.orderservice.model.Orders;
import com.innowise.orderservice.model.dto.request.OrderItemRequestDto;
import com.innowise.orderservice.repository.OrderItemsRepository;
import com.innowise.orderservice.service.ItemsService;
import com.innowise.orderservice.service.OrderItemsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class OrderItemsServiceImpl implements OrderItemsService {

  private final OrderItemsRepository orderItemsRepository;
  private final ItemsService itemsService;

  @Transactional
  @Override
  public List<OrderItems> createOrderItems(Orders order, List<OrderItemRequestDto> requestItems) {
    List<Items> items = itemsService.getItems(requestItems);

    Map<Long,Integer> itemsQuantity = requestItems.stream()
            .collect(Collectors.toMap(
                    OrderItemRequestDto::getItemId,
                    OrderItemRequestDto::getQuantity
            ));

    return items.stream()
            .map(item ->
                    OrderItems.builder()
                            .order(order)
                            .item(item)
                            .quantity(itemsQuantity.get(item.getId()))
                            .build()
            ).toList();
  }
}
