package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.exceptions.ItemWithThatNameAlreadyExistsException;
import com.innowise.orderservice.exceptions.ResourceNotFoundException;
import com.innowise.orderservice.mapper.ItemMapper;
import com.innowise.orderservice.model.Items;
import com.innowise.orderservice.model.dto.request.ItemsRequestDto;
import com.innowise.orderservice.model.dto.request.OrderItemRequestDto;
import com.innowise.orderservice.model.dto.response.ItemsResponseDto;
import com.innowise.orderservice.repository.ItemsRepository;
import com.innowise.orderservice.service.ItemsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Transactional
@Service
public class ItemServiceImpl implements ItemsService {

  private final ItemsRepository itemsRepository;
  private final ItemMapper itemMapper;

  @Override
  @Transactional(readOnly = true)
  public List<Items> getItems(List<OrderItemRequestDto> orderItemRequestDtos) {
    List<Long> itemsIds = orderItemRequestDtos
            .stream()
            .map(OrderItemRequestDto::getItemId)
            .distinct()
            .toList();

    List<Items> items = itemsRepository.findAllById(itemsIds);

    if (items.size() != itemsIds.size()) {
      List<Long> foundIds = items
              .stream()
              .map(Items::getId)
              .toList();
      List<Long> missingIds = itemsIds.stream()
              .filter(id -> !foundIds.contains(id))
              .toList();

      throw new ResourceNotFoundException("Items", "ids", missingIds);
    }
    return items;
  }

  @Override
  public ItemsResponseDto createItem(ItemsRequestDto itemsRequestDto) {
    Items items = itemMapper.toEntity(itemsRequestDto);
    if (existsByName(itemsRequestDto.getName())) {
      throw new ItemWithThatNameAlreadyExistsException("Item already exists with name "+itemsRequestDto.getName());
    }
    Items savedItem = itemsRepository.save(items);
    return itemMapper.toResponseDto(savedItem);
  }

  public boolean existsByName(String name) {
    return itemsRepository.existsByNameIgnoreCase(name.toLowerCase());
  }

  @Override
  public ItemsResponseDto updateItemById(ItemsRequestDto itemsRequestDto, long id) {
    Items newItem = itemsRepository
            .findById(id)
            .orElseThrow(()-> new ResourceNotFoundException("Item","id",id));

    if (existsByName(itemsRequestDto.getName()) && !newItem.getName().equals(itemsRequestDto.getName())) {
      throw new ItemWithThatNameAlreadyExistsException("Item already exists with name"+itemsRequestDto.getName());
    }
    itemMapper.updateEntityFromDto(itemsRequestDto, newItem);
    return itemMapper.toResponseDto(newItem);
  }

  @Override
  public void deleteItemById(long id) {
    Items item = itemsRepository
            .findById(id)
            .orElseThrow(()-> new ResourceNotFoundException("Item","id",id));
    itemsRepository.delete(item);
  }

  @Override
  public ItemsResponseDto getItemById(long id) {
    Items item = itemsRepository
            .findById(id)
            .orElseThrow(()-> new ResourceNotFoundException("Item","id",id));
    return itemMapper.toResponseDto(item);
  }


}
