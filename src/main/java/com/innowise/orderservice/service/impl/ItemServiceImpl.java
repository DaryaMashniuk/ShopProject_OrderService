package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.model.Items;
import com.innowise.orderservice.model.dto.OrderItemRequestDto;
import com.innowise.orderservice.model.dto.OrderRequestDto;
import com.innowise.orderservice.repository.ItemsRepository;
import com.innowise.orderservice.service.ItemsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
@Service
public class ItemServiceImpl implements ItemsService {

  private final ItemsRepository itemsRepository;

  @Override
  public List<Items> getItems(List<OrderItemRequestDto> orderItemRequestDtos) {
    List<Long> itemsIds = orderItemRequestDtos
            .stream()
            .map(OrderItemRequestDto::getItemId)
            .toList();

    List<Items> items = itemsRepository.findAllById(itemsIds);

    if (items.size() != itemsIds.size()) {
      throw new NoSuchElementException("Some items do not exist");
    }
    return items;
  }
}
