package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.model.Items;
import com.innowise.orderservice.model.OrderItems;
import com.innowise.orderservice.model.Orders;
import com.innowise.orderservice.model.dto.request.OrderItemRequestDto;
import com.innowise.orderservice.repository.OrderItemsRepository;
import com.innowise.orderservice.service.ItemsService;
import org.junit.jupiter.api.Assertions;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@DisplayName("OrderItemsServiceImpl Unit Tests")
class OrderItemsServiceImplTest {

  @Mock
  private OrderItemsRepository orderItemsRepository;

  @Mock
  private ItemsService itemsService;

  @InjectMocks
  private OrderItemsServiceImpl orderItemsService;

  private Orders testOrder;
  private Items testItem;
  private Items testItem2;
  private List<OrderItemRequestDto> orderItemRequestDtos;
  private OrderItemRequestDto orderItemRequestDto1;
  private OrderItemRequestDto orderItemRequestDto2;

  @BeforeEach
  void setUp() {
    testOrder = Orders.builder()
            .id(1L)
            .userId(100L)
            .build();

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

    orderItemRequestDto1 = OrderItemRequestDto.builder()
            .itemId(1L)
            .quantity(2)
            .build();

    orderItemRequestDto2 = OrderItemRequestDto.builder()
            .itemId(2L)
            .quantity(1)
            .build();

    orderItemRequestDtos = Arrays.asList(orderItemRequestDto1, orderItemRequestDto2);
  }

  @Nested
  @DisplayName("Create Order Items Tests")
  class CreateOrderItemsTests {

    @Test
    @DisplayName("Should create order items successfully")
    void shouldCreateOrderItemsSuccessfully() {
      List<Items> foundItems = Arrays.asList(testItem, testItem2);

      when(itemsService.getItems(orderItemRequestDtos)).thenReturn(foundItems);

      List<OrderItems> result = orderItemsService.createOrderItems(testOrder, orderItemRequestDtos);

      assertNotNull(result);
      assertEquals(2, result.size());

      OrderItems firstItem = result.get(0);
      assertEquals(testOrder, firstItem.getOrder());
      assertEquals(testItem, firstItem.getItem());
      assertEquals(2, firstItem.getQuantity());

      OrderItems secondItem = result.get(1);
      assertEquals(testOrder, secondItem.getOrder());
      assertEquals(testItem2, secondItem.getItem());
      assertEquals(1, secondItem.getQuantity());

      verify(itemsService).getItems(orderItemRequestDtos);
      verify(orderItemsRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Should create order items with correct quantity mapping")
    void shouldCreateOrderItemsWithCorrectQuantities() {
      List<Items> foundItems = Arrays.asList(testItem, testItem2);

      when(itemsService.getItems(orderItemRequestDtos)).thenReturn(foundItems);

      List<OrderItems> result = orderItemsService.createOrderItems(testOrder, orderItemRequestDtos);

      Assertions.assertEquals(2, result.stream()
              .filter(oi -> oi.getItem().getId().equals(1L))
              .findFirst()
              .get()
              .getQuantity());

      Assertions.assertEquals(1, result.stream()
              .filter(oi -> oi.getItem().getId().equals(2L))
              .findFirst()
              .get()
              .getQuantity());

      verify(itemsService).getItems(orderItemRequestDtos);
    }

    @Test
    @DisplayName("Should handle single order item")
    void shouldHandleSingleOrderItem() {
      List<OrderItemRequestDto> singleItemRequest = List.of(
              OrderItemRequestDto.builder()
                      .itemId(1L)
                      .quantity(3)
                      .build()
      );

      List<Items> foundItems = List.of(testItem);

      when(itemsService.getItems(singleItemRequest)).thenReturn(foundItems);

      List<OrderItems> result = orderItemsService.createOrderItems(testOrder, singleItemRequest);

      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals(testOrder, result.get(0).getOrder());
      assertEquals(testItem, result.get(0).getItem());
      assertEquals(3, result.get(0).getQuantity());

      verify(itemsService).getItems(singleItemRequest);
    }

    @Test
    @DisplayName("Should return empty list when request list is empty")
    void shouldReturnEmptyListWhenRequestEmpty() {
      List<OrderItemRequestDto> emptyRequest = List.of();
      List<Items> emptyItems = List.of();

      when(itemsService.getItems(emptyRequest)).thenReturn(emptyItems);

      List<OrderItems> result = orderItemsService.createOrderItems(testOrder, emptyRequest);

      assertNotNull(result);
      assertTrue(result.isEmpty());

      verify(itemsService).getItems(emptyRequest);
    }

    @Test
    @DisplayName("Should maintain order of items as returned by ItemsService")
    void shouldMaintainItemOrder() {
      List<Items> foundItems = Arrays.asList(testItem2, testItem);

      when(itemsService.getItems(orderItemRequestDtos)).thenReturn(foundItems);

      List<OrderItems> result = orderItemsService.createOrderItems(testOrder, orderItemRequestDtos);

      assertEquals(2, result.size());
      assertEquals(testItem2.getId(), result.get(0).getItem().getId());
      assertEquals(testItem.getId(), result.get(1).getItem().getId());

      assertEquals(1, result.get(0).getQuantity());
      assertEquals(2, result.get(1).getQuantity());

      verify(itemsService).getItems(orderItemRequestDtos);
    }

    @Test
    @DisplayName("Should throw exception when ItemsService throws exception")
    void shouldPropagateExceptionFromItemsService() {
      when(itemsService.getItems(orderItemRequestDtos))
              .thenThrow(new RuntimeException("Items service error"));

      RuntimeException exception = assertThrows(
              RuntimeException.class,
              () -> orderItemsService.createOrderItems(testOrder, orderItemRequestDtos)
      );

      assertEquals("Items service error", exception.getMessage());
      verify(itemsService).getItems(orderItemRequestDtos);
      verify(orderItemsRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Should create order items with correct mapping for duplicate item IDs")
    void shouldHandleDuplicateItemIds() {
      List<OrderItemRequestDto> duplicateRequests = Arrays.asList(
              OrderItemRequestDto.builder().itemId(1L).quantity(2).build(),
              OrderItemRequestDto.builder().itemId(1L).quantity(3).build()
      );

      List<Items> foundItems = Arrays.asList(testItem, testItem);

      when(itemsService.getItems(duplicateRequests)).thenReturn(foundItems);

      List<OrderItems> result = orderItemsService.createOrderItems(testOrder, duplicateRequests);

      assertEquals(2, result.size());
      assertEquals(5, result.get(0).getQuantity());
      assertEquals(testItem, result.get(0).getItem());

      verify(itemsService).getItems(duplicateRequests);
    }

    @Test
    @DisplayName("Should verify that each OrderItem has correct relationship with Order")
    void shouldVerifyOrderRelationship() {
      List<Items> foundItems = Arrays.asList(testItem, testItem2);

      when(itemsService.getItems(orderItemRequestDtos)).thenReturn(foundItems);

      List<OrderItems> result = orderItemsService.createOrderItems(testOrder, orderItemRequestDtos);

      result.forEach(orderItem -> {
        assertEquals(testOrder, orderItem.getOrder());
      });

      verify(itemsService).getItems(orderItemRequestDtos);
    }

    @Test
    @DisplayName("Should correctly handle large quantities")
    void shouldHandleLargeQuantities() {
      List<OrderItemRequestDto> largeQuantityRequest = List.of(
              OrderItemRequestDto.builder()
                      .itemId(1L)
                      .quantity(10000)
                      .build()
      );

      List<Items> foundItems = List.of(testItem);

      when(itemsService.getItems(largeQuantityRequest)).thenReturn(foundItems);

      List<OrderItems> result = orderItemsService.createOrderItems(testOrder, largeQuantityRequest);

      assertEquals(1, result.size());
      assertEquals(10000, result.get(0).getQuantity());

      verify(itemsService).getItems(largeQuantityRequest);
    }

    @Test
    @DisplayName("Should create new instances each time")
    void shouldCreateNewInstancesEachTime() {
      List<Items> foundItems = Arrays.asList(testItem, testItem2);

      when(itemsService.getItems(orderItemRequestDtos)).thenReturn(foundItems);

      List<OrderItems> result1 = orderItemsService.createOrderItems(testOrder, orderItemRequestDtos);
      List<OrderItems> result2 = orderItemsService.createOrderItems(testOrder, orderItemRequestDtos);

      assertNotSame(result1.get(0), result2.get(0));
      assertNotSame(result1.get(1), result2.get(1));

      assertEquals(result1.get(0).getQuantity(), result2.get(0).getQuantity());
      assertEquals(result1.get(1).getQuantity(), result2.get(1).getQuantity());

      verify(itemsService, times(2)).getItems(orderItemRequestDtos);
    }
  }
}