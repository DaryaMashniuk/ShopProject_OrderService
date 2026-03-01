package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.exceptions.ItemWithThatNameAlreadyExistsException;
import com.innowise.orderservice.exceptions.ResourceNotFoundException;
import com.innowise.orderservice.mapper.ItemMapper;
import com.innowise.orderservice.model.Items;
import com.innowise.orderservice.model.dto.request.ItemsRequestDto;
import com.innowise.orderservice.model.dto.request.OrderItemRequestDto;
import com.innowise.orderservice.model.dto.response.ItemsResponseDto;
import com.innowise.orderservice.repository.ItemsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ItemServiceImpl Unit Tests")
class ItemServiceImplTest {

  @Mock
  private ItemsRepository itemsRepository;

  @Mock
  private ItemMapper itemMapper;

  @InjectMocks
  private ItemServiceImpl itemService;


  private Items testItem;
  private Items testItem2;
  private ItemsResponseDto testItemResponseDto;
  private ItemsRequestDto testItemRequestDto;
  private List<OrderItemRequestDto> orderItemRequestDtos;

  @BeforeEach
  void setUp() {
    testItem = Items.builder()
            .id(1L)
            .name("Laptop")
            .price(new BigDecimal("999.99"))
            .build();

    testItem2 = Items.builder()
            .id(2L)
            .name("Mouse")
            .price(new BigDecimal("29.99"))
            .build();

    testItemResponseDto = ItemsResponseDto.builder()
            .id(1L)
            .name("Laptop")
            .price(new BigDecimal("999.99"))
            .build();

    testItemRequestDto = ItemsRequestDto.builder()
            .name("Laptop")
            .price(new BigDecimal("999.99"))
            .build();

    orderItemRequestDtos = Arrays.asList(
            OrderItemRequestDto.builder().itemId(1L).quantity(2).build(),
            OrderItemRequestDto.builder().itemId(2L).quantity(1).build()
    );
  }

  @Nested
  @DisplayName("Get Items Tests")
  class GetItemsTests {

    @Test
    @DisplayName("Should get items by IDs successfully")
    void shouldGetItemsSuccessfully() {
      List<Long> expectedIds = Arrays.asList(1L, 2L);
      List<Items> expectedItems = Arrays.asList(testItem, testItem2);

      when(itemsRepository.findAllById(expectedIds)).thenReturn(expectedItems);

      List<Items> result = itemService.getItems(orderItemRequestDtos);

      assertNotNull(result);
      assertEquals(2, result.size());
      assertEquals(testItem.getId(), result.get(0).getId());
      assertEquals(testItem2.getId(), result.get(1).getId());

      verify(itemsRepository).findAllById(expectedIds);
    }

    @Test
    @DisplayName("Should throw NoSuchElementException when some items not found")
    void shouldThrowExceptionWhenItemsNotFound() {
      List<Long> requestedIds = Arrays.asList(1L, 2L, 3L);
      List<Items> foundItems = Arrays.asList(testItem, testItem2);

      List<OrderItemRequestDto> requestWithThreeItems = Arrays.asList(
              OrderItemRequestDto.builder().itemId(1L).quantity(2).build(),
              OrderItemRequestDto.builder().itemId(2L).quantity(1).build(),
              OrderItemRequestDto.builder().itemId(3L).quantity(1).build()
      );

      when(itemsRepository.findAllById(requestedIds)).thenReturn(foundItems);

      assertThrows(
              ResourceNotFoundException.class,
              () -> itemService.getItems(requestWithThreeItems)
      );

      verify(itemsRepository).findAllById(anyList());
    }

    @Test
    @DisplayName("Should return empty list when request list is empty")
    void shouldReturnEmptyListWhenRequestEmpty() {
      List<OrderItemRequestDto> emptyRequest = List.of();

      List<Items> result = itemService.getItems(emptyRequest);

      assertNotNull(result);
      assertTrue(result.isEmpty());
      verify(itemsRepository).findAllById(emptyList());
    }
  }

  @Nested
  @DisplayName("Create Item Tests")
  class CreateItemTests {

    @Test
    @DisplayName("Should create item successfully")
    void shouldCreateItemSuccessfully() {
      when(itemMapper.toEntity(testItemRequestDto)).thenReturn(testItem);
      when(itemsRepository.existsByNameIgnoreCase(testItemRequestDto.getName().toLowerCase()))
              .thenReturn(false);
      when(itemsRepository.save(any(Items.class))).thenReturn(testItem);
      when(itemMapper.toResponseDto(testItem)).thenReturn(testItemResponseDto);

      ItemsResponseDto result = itemService.createItem(testItemRequestDto);

      assertNotNull(result);
      assertEquals(testItemResponseDto.getId(), result.getId());
      assertEquals(testItemResponseDto.getName(), result.getName());

      verify(itemMapper).toEntity(testItemRequestDto);
      verify(itemsRepository).existsByNameIgnoreCase(testItemRequestDto.getName().toLowerCase());
      verify(itemsRepository).save(testItem);
      verify(itemMapper).toResponseDto(testItem);
    }

    @Test
    @DisplayName("Should throw ItemWithThatNameAlreadyExistsException when name exists")
    void shouldThrowExceptionWhenNameExists() {
      when(itemMapper.toEntity(testItemRequestDto)).thenReturn(testItem);
      when(itemsRepository.existsByNameIgnoreCase(testItemRequestDto.getName().toLowerCase()))
              .thenReturn(true);

      ItemWithThatNameAlreadyExistsException exception = assertThrows(
              ItemWithThatNameAlreadyExistsException.class,
              () -> itemService.createItem(testItemRequestDto)
      );

      assertTrue(exception.getMessage().contains(testItemRequestDto.getName()));
      verify(itemMapper).toEntity(testItemRequestDto);
      verify(itemsRepository).existsByNameIgnoreCase(testItemRequestDto.getName().toLowerCase());
      verify(itemsRepository, never()).save(any());
      verify(itemMapper, never()).toResponseDto(any());
    }
  }

  @Nested
  @DisplayName("Exists By Name Tests")
  class ExistsByNameTests {

    @Test
    @DisplayName("Should return true when item exists with name")
    void shouldReturnTrueWhenExists() {
      String name = "Laptop";
      when(itemsRepository.existsByNameIgnoreCase(name.toLowerCase())).thenReturn(true);

      boolean result = itemService.existsByName(name);

      assertTrue(result);
      verify(itemsRepository).existsByNameIgnoreCase(name.toLowerCase());
    }

    @Test
    @DisplayName("Should return false when item does not exist with name")
    void shouldReturnFalseWhenNotExists() {
      String name = "NonExistent";
      when(itemsRepository.existsByNameIgnoreCase(name.toLowerCase())).thenReturn(false);

      boolean result = itemService.existsByName(name);

      assertFalse(result);
      verify(itemsRepository).existsByNameIgnoreCase(name.toLowerCase());
    }
  }

  @Nested
  @DisplayName("Update Item Tests")
  class UpdateItemTests {

    @Test
    @DisplayName("Should update item successfully when name not changed")
    void shouldUpdateItemSuccessfullyWhenNameNotChanged() {
      Long itemId = 1L;
      ItemsRequestDto updateRequest = ItemsRequestDto.builder()
              .name("Laptop")
              .price(new BigDecimal("899.99"))
              .build();

      Items existingItem = Items.builder()
              .id(1L)
              .name("Laptop")
              .price(new BigDecimal("999.99"))
              .build();

      ItemsResponseDto expectedResponse = ItemsResponseDto.builder()
              .id(1L)
              .name("Laptop")
              .price(new BigDecimal("899.99"))
              .build();

      when(itemsRepository.findById(itemId)).thenReturn(Optional.of(existingItem));

      when(itemsRepository.existsByNameIgnoreCase(updateRequest.getName().toLowerCase()))
              .thenReturn(true);
      doNothing().when(itemMapper).updateEntityFromDto(updateRequest, existingItem);
      when(itemMapper.toResponseDto(any(Items.class))).thenReturn(expectedResponse);

      ItemsResponseDto result = itemService.updateItemById(updateRequest, itemId);

      assertNotNull(result);
      assertEquals(expectedResponse.getId(), result.getId());
      assertEquals(expectedResponse.getPrice(), result.getPrice());

      verify(itemsRepository).findById(itemId);
      verify(itemsRepository).existsByNameIgnoreCase(updateRequest.getName().toLowerCase());
      verify(itemMapper).updateEntityFromDto(updateRequest, existingItem);
      verify(itemMapper).toResponseDto(existingItem);
    }

    @Test
    @DisplayName("Should update item successfully when name changed and new name not taken")
    void shouldUpdateItemSuccessfullyWhenNameChanged() {
      Long itemId = 1L;
      ItemsRequestDto updateRequest = ItemsRequestDto.builder()
              .name("Gaming Laptop")
              .price(new BigDecimal("1299.99"))
              .build();

      Items existingItem = Items.builder()
              .id(1L)
              .name("Laptop")
              .price(new BigDecimal("999.99"))
              .build();

      when(itemsRepository.findById(itemId)).thenReturn(Optional.of(existingItem));
      when(itemsRepository.existsByNameIgnoreCase(updateRequest.getName().toLowerCase()))
              .thenReturn(false);
      doNothing().when(itemMapper).updateEntityFromDto(updateRequest, existingItem);
      when(itemMapper.toResponseDto(existingItem)).thenReturn(
              ItemsResponseDto.builder()
                      .id(1L)
                      .name("Gaming Laptop")
                      .price(new BigDecimal("1299.99"))
                      .build()
      );

      ItemsResponseDto result = itemService.updateItemById(updateRequest, itemId);

      assertNotNull(result);
      assertEquals("Gaming Laptop", result.getName());
      assertEquals(new BigDecimal("1299.99"), result.getPrice());

      verify(itemsRepository).findById(itemId);
      verify(itemsRepository).existsByNameIgnoreCase(updateRequest.getName().toLowerCase());
      verify(itemMapper).updateEntityFromDto(updateRequest, existingItem);
    }

    @Test
    @DisplayName("Should throw ItemWithThatNameAlreadyExistsException when new name already taken")
    void shouldThrowExceptionWhenNewNameTaken() {
      Long itemId = 1L;
      ItemsRequestDto updateRequest = ItemsRequestDto.builder()
              .name("Mouse")
              .price(new BigDecimal("39.99"))
              .build();

      Items existingItem = Items.builder()
              .id(1L)
              .name("Laptop")
              .price(new BigDecimal("999.99"))
              .build();

      when(itemsRepository.findById(itemId)).thenReturn(Optional.of(existingItem));
      when(itemsRepository.existsByNameIgnoreCase(updateRequest.getName().toLowerCase()))
              .thenReturn(true);

      ItemWithThatNameAlreadyExistsException exception = assertThrows(
              ItemWithThatNameAlreadyExistsException.class,
              () -> itemService.updateItemById(updateRequest, itemId)
      );

      assertTrue(exception.getMessage().contains(updateRequest.getName()));
      verify(itemsRepository).findById(itemId);
      verify(itemsRepository).existsByNameIgnoreCase(updateRequest.getName().toLowerCase());
      verify(itemMapper, never()).updateEntityFromDto(any(), any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when item not found")
    void shouldThrowExceptionWhenItemNotFound() {
      Long itemId = 999L;
      when(itemsRepository.findById(itemId)).thenReturn(Optional.empty());

      ResourceNotFoundException exception = assertThrows(
              ResourceNotFoundException.class,
              () -> itemService.updateItemById(testItemRequestDto, itemId)
      );

      assertEquals("Item not found with id: '999'", exception.getMessage());
      verify(itemsRepository).findById(itemId);
      verify(itemsRepository, never()).existsByNameIgnoreCase(anyString());
      verify(itemMapper, never()).updateEntityFromDto(any(), any());
    }
  }

  @Nested
  @DisplayName("Delete Item Tests")
  class DeleteItemTests {

    @Test
    @DisplayName("Should delete item successfully")
    void shouldDeleteItemSuccessfully() {
      Long itemId = 1L;
      when(itemsRepository.findById(itemId)).thenReturn(Optional.of(testItem));

      itemService.deleteItemById(itemId);

      verify(itemsRepository).findById(itemId);
      verify(itemsRepository).delete(testItem);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent item")
    void shouldThrowExceptionWhenDeletingNonExistentItem() {
      Long itemId = 999L;
      when(itemsRepository.findById(itemId)).thenReturn(Optional.empty());

      ResourceNotFoundException exception = assertThrows(
              ResourceNotFoundException.class,
              () -> itemService.deleteItemById(itemId)
      );

      assertEquals("Item not found with id: '999'", exception.getMessage());
      verify(itemsRepository).findById(itemId);
      verify(itemsRepository, never()).delete(any());
    }
  }

  @Nested
  @DisplayName("Get Item By ID Tests")
  class GetItemByIdTests {

    @Test
    @DisplayName("Should get item by id successfully")
    void shouldGetItemByIdSuccessfully() {
      Long itemId = 1L;
      when(itemsRepository.findById(itemId)).thenReturn(Optional.of(testItem));
      when(itemMapper.toResponseDto(testItem)).thenReturn(testItemResponseDto);

      ItemsResponseDto result = itemService.getItemById(itemId);

      assertNotNull(result);
      assertEquals(testItemResponseDto.getId(), result.getId());
      assertEquals(testItemResponseDto.getName(), result.getName());

      verify(itemsRepository).findById(itemId);
      verify(itemMapper).toResponseDto(testItem);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when item not found")
    void shouldThrowExceptionWhenItemNotFound() {
      Long itemId = 999L;
      when(itemsRepository.findById(itemId)).thenReturn(Optional.empty());

      ResourceNotFoundException exception = assertThrows(
              ResourceNotFoundException.class,
              () -> itemService.getItemById(itemId)
      );

      assertEquals("Item not found with id: '999'", exception.getMessage());
      verify(itemsRepository).findById(itemId);
      verify(itemMapper, never()).toResponseDto(any());
    }
  }
}