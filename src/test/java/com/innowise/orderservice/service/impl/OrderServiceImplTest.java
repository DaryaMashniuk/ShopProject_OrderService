package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.exceptions.CannotUpdateWithStatusException;
import com.innowise.orderservice.exceptions.ResourceNotFoundException;
import com.innowise.orderservice.model.Items;
import com.innowise.orderservice.model.OrderItems;
import com.innowise.orderservice.model.OrderStatus;
import com.innowise.orderservice.model.Orders;
import com.innowise.orderservice.model.dto.request.OrderItemRequestDto;
import com.innowise.orderservice.model.dto.request.OrderSearchCriteriaDto;
import com.innowise.orderservice.model.dto.request.OrderUpdateDto;
import com.innowise.orderservice.repository.OrdersRepository;
import com.innowise.orderservice.service.OrderItemsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@DisplayName("OrderServiceImpl Unit Tests")
class OrderServiceImplTest {

  @Mock
  private OrdersRepository ordersRepository;

  @Mock
  private OrderItemsService orderItemsService;

  @InjectMocks
  private OrderServiceImpl orderService;

  private Orders testOrder;
  private Orders testOrder2;
  private OrderItems testOrderItem;
  private OrderItems testOrderItem2;
  private Items testItem;
  private Items testItem2;
  private List<OrderItemRequestDto> orderItemRequestDtos;
  private OrderItemRequestDto orderItemRequestDto1;
  private OrderItemRequestDto orderItemRequestDto2;
  private OrderUpdateDto orderUpdateDto;
  private OrderSearchCriteriaDto searchCriteria;
  private Pageable pageable;
  private Page<Orders> orderPage;

  @BeforeEach
  void setUp() {
    LocalDateTime now = LocalDateTime.now();

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

    testOrderItem = OrderItems.builder()
            .id(1L)
            .item(testItem)
            .quantity(2)
            .build();

    testOrderItem2 = OrderItems.builder()
            .id(2L)
            .item(testItem2)
            .quantity(1)
            .build();

    List<OrderItems> orderItems = new ArrayList<>(Arrays.asList(testOrderItem, testOrderItem2));

    testOrder = Orders.builder()
            .id(1L)
            .userId(100L)
            .status(OrderStatus.PENDING)
            .totalPrice(new BigDecimal("2029.97"))
            .orderItems(orderItems)
            .deleted(false)
            .build();
    testOrder.setCreatedAt(now);
    testOrder.setUpdatedAt(now);

    testOrder2 = Orders.builder()
            .id(2L)
            .userId(100L)
            .status(OrderStatus.APPROVED)
            .totalPrice(new BigDecimal("59.98"))
            .orderItems(Collections.singletonList(testOrderItem2))
            .deleted(false)
            .build();
    testOrder2.setCreatedAt(now.minusDays(1));

    orderItemRequestDto1 = OrderItemRequestDto.builder()
            .itemId(1L)
            .quantity(2)
            .build();

    orderItemRequestDto2 = OrderItemRequestDto.builder()
            .itemId(2L)
            .quantity(1)
            .build();

    orderItemRequestDtos = Arrays.asList(orderItemRequestDto1, orderItemRequestDto2);

    List<OrderItemRequestDto> updatedItems = Collections.singletonList(
            OrderItemRequestDto.builder()
                    .itemId(1L)
                    .quantity(3)
                    .build()
    );

    orderUpdateDto = OrderUpdateDto.builder()
            .status("APPROVED")
            .items(updatedItems)
            .build();

    searchCriteria = OrderSearchCriteriaDto.builder()
            .fromDate(now.minusDays(2))
            .toDate(now)
            .status(Collections.singletonList(String.valueOf(OrderStatus.PENDING)))
            .build();

    pageable = PageRequest.of(0, 10);
    orderPage = new PageImpl<>(Collections.singletonList(testOrder), pageable, 1);
  }

  @Nested
  @DisplayName("Create Order Tests")
  class CreateOrderTests {

    @Test
    @DisplayName("Should create order successfully with valid items")
    void shouldCreateOrderSuccessfully() {
      Long userId = 100L;

      when(orderItemsService.createOrderItems(any(Orders.class), eq(orderItemRequestDtos)))
              .thenReturn(Arrays.asList(testOrderItem, testOrderItem2));

      when(ordersRepository.save(any(Orders.class))).thenAnswer(invocation -> {
        Orders orderToSave = invocation.getArgument(0);
        orderToSave.setId(1L);
        return orderToSave;
      });

      Orders createdOrder = orderService.createOrder(orderItemRequestDtos, userId);

      assertNotNull(createdOrder);
      assertEquals(userId, createdOrder.getUserId());
      assertEquals(OrderStatus.PENDING, createdOrder.getStatus());
      assertEquals(new BigDecimal("2029.97"), createdOrder.getTotalPrice());
      assertEquals(2, createdOrder.getOrderItems().size());

      verify(orderItemsService).createOrderItems(any(Orders.class), eq(orderItemRequestDtos));
      verify(ordersRepository).save(any(Orders.class));
    }

    @Test
    @DisplayName("Should create order with empty items list")
    void shouldCreateOrderWithEmptyItems() {
      Long userId = 100L;
      List<OrderItemRequestDto> emptyItems = Collections.emptyList();

      when(orderItemsService.createOrderItems(any(Orders.class), eq(emptyItems)))
              .thenReturn(Collections.emptyList());

      when(ordersRepository.save(any(Orders.class))).thenAnswer(invocation -> {
        Orders orderToSave = invocation.getArgument(0);
        orderToSave.setId(2L);
        return orderToSave;
      });

      Orders createdOrder = orderService.createOrder(emptyItems, userId);

      assertNotNull(createdOrder);
      assertEquals(userId, createdOrder.getUserId());
      assertEquals(OrderStatus.PENDING, createdOrder.getStatus());
      assertEquals(BigDecimal.ZERO, createdOrder.getTotalPrice());
      assertTrue(createdOrder.getOrderItems().isEmpty());

      verify(orderItemsService).createOrderItems(any(Orders.class), eq(emptyItems));
      verify(ordersRepository).save(any(Orders.class));
    }

    @Test
    @DisplayName("Should calculate total price correctly for multiple items")
    void shouldCalculateTotalPriceCorrectly() {
      Long userId = 100L;

      when(orderItemsService.createOrderItems(any(Orders.class), eq(orderItemRequestDtos)))
              .thenReturn(Arrays.asList(testOrderItem, testOrderItem2));

      when(ordersRepository.save(any(Orders.class))).thenAnswer(invocation -> invocation.getArgument(0));

      Orders createdOrder = orderService.createOrder(orderItemRequestDtos, userId);

      BigDecimal expectedTotal = new BigDecimal("2029.97");
      assertEquals(expectedTotal, createdOrder.getTotalPrice());

      verify(orderItemsService).createOrderItems(any(Orders.class), eq(orderItemRequestDtos));
      verify(ordersRepository).save(any(Orders.class));
    }
  }

  @Nested
  @DisplayName("Get Order Tests")
  class GetOrderTests {

    @Test
    @DisplayName("Should get order by id successfully")
    void shouldGetOrderByIdSuccessfully() {
      Long orderId = 1L;

      when(ordersRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

      Orders foundOrder = orderService.getOrderById(orderId);

      assertNotNull(foundOrder);
      assertEquals(orderId, foundOrder.getId());
      assertEquals(testOrder.getUserId(), foundOrder.getUserId());
      assertEquals(testOrder.getTotalPrice(), foundOrder.getTotalPrice());

      verify(ordersRepository).findById(orderId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when order not found by id")
    void shouldThrowExceptionWhenOrderNotFound() {
      Long orderId = 999L;

      when(ordersRepository.findById(orderId)).thenReturn(Optional.empty());

      ResourceNotFoundException exception = assertThrows(
              ResourceNotFoundException.class,
              () -> orderService.getOrderById(orderId)
      );

      assertEquals("Order not found with id: '999'", exception.getMessage());
      verify(ordersRepository).findById(orderId);
    }
  }

  @Nested
  @DisplayName("Get Orders By User ID Tests")
  class GetOrdersByUserIdTests {

    @Test
    @DisplayName("Should get orders by user id successfully")
    void shouldGetOrdersByUserIdSuccessfully() {
      Long userId = 100L;
      List<Orders> expectedOrders = Arrays.asList(testOrder, testOrder2);

      when(ordersRepository.findByUserId(userId)).thenReturn(expectedOrders);

      List<Orders> foundOrders = orderService.getOrdersByUserId(userId);

      assertNotNull(foundOrders);
      assertEquals(2, foundOrders.size());
      assertEquals(testOrder.getId(), foundOrders.get(0).getId());
      assertEquals(testOrder2.getId(), foundOrders.get(1).getId());

      verify(ordersRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("Should return empty list when user exists but has no orders")
    void shouldReturnEmptyListWhenUserHasNoOrders() {
      Long userId = 200L;

      when(ordersRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

      List<Orders> foundOrders = orderService.getOrdersByUserId(userId);

      assertNotNull(foundOrders);
      assertTrue(foundOrders.isEmpty());

      verify(ordersRepository).findByUserId(userId);
    }
  }

  @Nested
  @DisplayName("Update Order Tests")
  class UpdateOrderTests {

    @Test
    @DisplayName("Should update order status and items successfully")
    void shouldUpdateOrderSuccessfully() {
      Long orderId = 1L;
      List<OrderItems> updatedItems = Collections.singletonList(
              OrderItems.builder()
                      .id(3L)
                      .item(testItem)
                      .quantity(3)
                      .build()
      );

      when(ordersRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
      when(orderItemsService.createOrderItems(eq(testOrder), anyList()))
              .thenReturn(updatedItems);

      Orders updatedOrder = orderService.updateOrderById(orderId, orderUpdateDto);

      assertNotNull(updatedOrder);
      assertEquals(OrderStatus.APPROVED, updatedOrder.getStatus());
      assertEquals(1, updatedOrder.getOrderItems().size());
      assertEquals(3, updatedOrder.getOrderItems().get(0).getQuantity());

      verify(ordersRepository).findById(orderId);
      verify(orderItemsService).createOrderItems(eq(testOrder), anyList());
    }
    @Test
    @DisplayName("Should update only status when items are null")
    void shouldUpdateOnlyStatusWhenItemsNull() {
      Long orderId = 1L;
      OrderUpdateDto statusOnlyUpdate = OrderUpdateDto.builder()
              .status("DELIVERED")
              .items(null)
              .build();

      when(ordersRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

      Orders updatedOrder = orderService.updateOrderById(orderId, statusOnlyUpdate);

      assertEquals(OrderStatus.DELIVERED, updatedOrder.getStatus());
      assertEquals(2, updatedOrder.getOrderItems().size());
      verify(orderItemsService, never()).createOrderItems(any(), anyList());
    }

    @Test
    @DisplayName("Should not update items when order status is not PENDING")
    void shouldNotUpdateItemsWhenOrderNotPending() {
      Long orderId = 2L;
      when(ordersRepository.findById(orderId)).thenReturn(Optional.of(testOrder2));

      CannotUpdateWithStatusException exception = assertThrows(
              CannotUpdateWithStatusException.class,
              () -> orderService.updateOrderById(orderId, orderUpdateDto)
      );

      assertEquals("Cannot update items for order in status: APPROVED", exception.getMessage());

      verify(orderItemsService, never()).createOrderItems(any(), anyList());
    }

    @Test
    @DisplayName("Should throw exception when order not found for update")
    void shouldThrowExceptionWhenOrderNotFoundForUpdate() {
      Long orderId = 999L;
      when(ordersRepository.findById(orderId)).thenReturn(Optional.empty());

      ResourceNotFoundException exception = assertThrows(
              ResourceNotFoundException.class,
              () -> orderService.updateOrderById(orderId, orderUpdateDto)
      );

      assertEquals("Order not found with id: '999'", exception.getMessage());
      verify(ordersRepository).findById(orderId);
      verify(orderItemsService, never()).createOrderItems(any(), anyList());
    }
  }

  @Nested
  @DisplayName("Delete Order Tests")
  class DeleteOrderTests {

    @Test
    @DisplayName("Should delete order successfully")
    void shouldDeleteOrderSuccessfully() {
      Long orderId = 1L;

      when(ordersRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

      orderService.deleteOrderById(orderId);

      verify(ordersRepository).findById(orderId);
      verify(ordersRepository).delete(testOrder);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent order")
    void shouldThrowExceptionWhenDeletingNonExistentOrder() {
      Long orderId = 999L;

      when(ordersRepository.findById(orderId)).thenReturn(Optional.empty());

      ResourceNotFoundException exception = assertThrows(
              ResourceNotFoundException.class,
              () -> orderService.deleteOrderById(orderId)
      );

      assertEquals("Order not found with id: '999'", exception.getMessage());
      verify(ordersRepository).findById(orderId);
      verify(ordersRepository, never()).delete((Orders) any());
    }
  }

  @Nested
  @DisplayName("Find All Orders Tests")
  class FindAllOrdersTests {

    @Test
    @DisplayName("Should find all orders without filters")
    void shouldFindAllOrdersWithoutFilters() {
      OrderSearchCriteriaDto emptyCriteria = OrderSearchCriteriaDto.builder().build();

      when(ordersRepository.findAll(pageable)).thenReturn(orderPage);

      Page<Orders> result = orderService.findAllOrders(emptyCriteria, pageable);

      assertNotNull(result);
      assertEquals(1, result.getTotalElements());
      verify(ordersRepository).findAll(pageable);
      verify(ordersRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Should find all orders with filters")
    void shouldFindAllOrdersWithFilters() {
      when(ordersRepository.findAll(any(Specification.class), eq(pageable)))
              .thenReturn(orderPage);

      Page<Orders> result = orderService.findAllOrders(searchCriteria, pageable);

      assertNotNull(result);
      assertEquals(1, result.getTotalElements());
      verify(ordersRepository).findAll(any(Specification.class), eq(pageable));
      verify(ordersRepository, never()).findAll(pageable);
    }

    @Test
    @DisplayName("Should return empty page when no orders match filters")
    void shouldReturnEmptyPageWhenNoOrdersMatchFilters() {
      Page<Orders> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

      when(ordersRepository.findAll(any(Specification.class), eq(pageable)))
              .thenReturn(emptyPage);

      Page<Orders> result = orderService.findAllOrders(searchCriteria, pageable);

      assertNotNull(result);
      assertEquals(0, result.getTotalElements());
      assertTrue(result.getContent().isEmpty());
      verify(ordersRepository).findAll(any(Specification.class), eq(pageable));
    }
  }

  @Nested
  @DisplayName("Calculate Total Price Tests")
  class CalculateTotalPriceTests {

    @Test
    @DisplayName("Should calculate total price correctly")
    void shouldCalculateTotalPriceCorrectly() {
      Long userId = 100L;

      when(orderItemsService.createOrderItems(any(Orders.class), eq(orderItemRequestDtos)))
              .thenReturn(Arrays.asList(testOrderItem, testOrderItem2));
      when(ordersRepository.save(any(Orders.class))).thenAnswer(invocation -> invocation.getArgument(0));

      Orders createdOrder = orderService.createOrder(orderItemRequestDtos, userId);

      BigDecimal expectedTotal = testItem.getPrice()
              .multiply(BigDecimal.valueOf(testOrderItem.getQuantity()))
              .add(testItem2.getPrice()
                      .multiply(BigDecimal.valueOf(testOrderItem2.getQuantity())));

      assertEquals(expectedTotal, createdOrder.getTotalPrice());
    }

    @Test
    @DisplayName("Should return zero for empty items list")
    void shouldReturnZeroForEmptyItems() {
      Long userId = 100L;
      List<OrderItemRequestDto> emptyItems = Collections.emptyList();

      when(orderItemsService.createOrderItems(any(Orders.class), eq(emptyItems)))
              .thenReturn(Collections.emptyList());
      when(ordersRepository.save(any(Orders.class))).thenAnswer(invocation -> invocation.getArgument(0));

      Orders createdOrder = orderService.createOrder(emptyItems, userId);

      assertEquals(BigDecimal.ZERO, createdOrder.getTotalPrice());
    }
  }
}