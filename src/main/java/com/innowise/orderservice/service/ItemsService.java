package com.innowise.orderservice.service;

import com.innowise.orderservice.model.Items;
import com.innowise.orderservice.model.dto.request.ItemsRequestDto;
import com.innowise.orderservice.model.dto.request.OrderItemRequestDto;
import com.innowise.orderservice.model.dto.response.ItemsResponseDto;

import java.util.List;

public interface ItemsService {
  List<Items> getItems(List<OrderItemRequestDto> orderItemRequestDtos);
  ItemsResponseDto createItem(ItemsRequestDto itemsRequestDto);
  ItemsResponseDto updateItemById(ItemsRequestDto itemsRequestDto, long id);
  void deleteItemById(long id);
  ItemsResponseDto getItemById(long id);
}
