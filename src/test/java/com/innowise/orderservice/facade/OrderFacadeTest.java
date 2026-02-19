package com.innowise.orderservice.facade;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.innowise.orderservice.BaseIntegrationTest;
import com.innowise.orderservice.model.Items;
import com.innowise.orderservice.model.OrderStatus;
import com.innowise.orderservice.model.Orders;
import com.innowise.orderservice.model.dto.request.OrderItemRequestDto;
import com.innowise.orderservice.model.dto.request.OrderRequestDto;
import com.innowise.orderservice.model.dto.request.OrderSearchCriteriaDto;
import com.innowise.orderservice.model.dto.request.OrderUpdateDto;
import com.innowise.orderservice.model.dto.response.OrderResponseDto;
import com.innowise.orderservice.model.dto.response.PageResponseDto;
import com.innowise.orderservice.model.dto.response.UserOrdersListResponseDto;
import com.innowise.orderservice.model.dto.response.UserResponseDto;
import com.innowise.orderservice.repository.ItemsRepository;
import com.innowise.orderservice.repository.OrdersRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
class OrderFacadeTest extends BaseIntegrationTest {

  @RegisterExtension
  static WireMockExtension wireMockExtension = WireMockExtension.newInstance()
          .options(wireMockConfig().port(8081))
          .build();

  @Autowired
  private OrderFacade orderFacade;

  @Autowired
  private OrdersRepository ordersRepository;

  @Autowired
  private ItemsRepository itemsRepository;

  @Autowired
  private ObjectMapper objectMapper;

  private Items testItem1;
  private Items testItem2;
  private OrderItemRequestDto itemRequest1;
  private OrderItemRequestDto itemRequest2;
  private OrderRequestDto orderRequest;
  private OrderUpdateDto orderUpdateRequest;
  private Pageable pageable;
  private Orders savedOrder;
  private Orders savedOrder2;
  private Long regularUserId = 100L;
  private Long otherUserId = 200L;
  private LocalDateTime now;

  @DynamicPropertySource
  static void overrideProperties(DynamicPropertyRegistry registry) {
    registry.add("user.service.url", wireMockExtension::baseUrl);
  }

  @Override
  protected WireMockExtension getWireMockExtension() {
    return wireMockExtension;
  }

  @BeforeEach
  void setUp() {
    ordersRepository.deleteAll();
    itemsRepository.deleteAll();

    wireMockExtension.resetAll();

    now = LocalDateTime.now();

    testItem1 = Items.builder()
            .name("Laptop")
            .price(new BigDecimal("999.99"))
            .build();
    testItem1 = itemsRepository.save(testItem1);

    testItem2 = Items.builder()
            .name("Mouse")
            .price(new BigDecimal("29.99"))
            .build();
    testItem2 = itemsRepository.save(testItem2);

    itemRequest1 = OrderItemRequestDto.builder()
            .itemId(testItem1.getId())
            .quantity(2)
            .build();

    itemRequest2 = OrderItemRequestDto.builder()
            .itemId(testItem2.getId())
            .quantity(1)
            .build();

    orderRequest = OrderRequestDto.builder()
            .email("john.doe@example.com")
            .items(Arrays.asList(itemRequest1, itemRequest2))
            .build();

    List<OrderItemRequestDto> updateItems = Collections.singletonList(
            OrderItemRequestDto.builder()
                    .itemId(testItem1.getId())
                    .quantity(3)
                    .build()
    );

    orderUpdateRequest = OrderUpdateDto.builder()
            .email("john.doe@example.com")
            .status("APPROVED")
            .items(updateItems)
            .build();

    pageable = PageRequest.of(0, 10);

    createTestOrders();

    setupUserServiceStubs();
  }

  private void setupUserServiceStubs() {
    wireMockExtension.stubFor(WireMock.get(urlPathEqualTo("/userservice/api/v1/users"))
            .withQueryParam("email", equalTo("john.doe@example.com"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("""
                                {
                                    "content": [{
                                        "id": 100,
                                        "name": "John",
                                        "surname": "Doe",
                                        "email": "john.doe@example.com"
                                    }],
                                    "currentPage": 0,
                                    "pageSize": 10,
                                    "totalElements": 1,
                                    "totalPages": 1,
                                    "first": true,
                                    "last": true
                                }
                                """)));

    wireMockExtension.stubFor(WireMock.get(urlPathEqualTo("/userservice/api/v1/users"))
            .withQueryParam("email", equalTo("jane.smith@example.com"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("""
                                {
                                    "content": [{
                                        "id": 200,
                                        "name": "Jane",
                                        "surname": "Smith",
                                        "email": "jane.smith@example.com"
                                    }],
                                    "currentPage": 0,
                                    "pageSize": 10,
                                    "totalElements": 1,
                                    "totalPages": 1,
                                    "first": true,
                                    "last": true
                                }
                                """)));

    wireMockExtension.stubFor(WireMock.get(urlEqualTo("/userservice/api/v1/users/100"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("""
                                {
                                    "id": 100,
                                    "name": "John",
                                    "surname": "Doe",
                                    "email": "john.doe@example.com"
                                }
                                """)));

    wireMockExtension.stubFor(WireMock.get(urlEqualTo("/userservice/api/v1/users/200"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("""
                                {
                                    "id": 200,
                                    "name": "Jane",
                                    "surname": "Smith",
                                    "email": "jane.smith@example.com"
                                }
                                """)));

    wireMockExtension.stubFor(WireMock.get(urlPathEqualTo("/userservice/api/v1/users/batch"))
            .withQueryParam("ids", matching(".*"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("""
                                [
                                    {
                                        "id": 100,
                                        "name": "John",
                                        "surname": "Doe",
                                        "email": "john.doe@example.com"
                                    },
                                    {
                                        "id": 200,
                                        "name": "Jane",
                                        "surname": "Smith",
                                        "email": "jane.smith@example.com"
                                    }
                                ]
                                """)));

    wireMockExtension.stubFor(WireMock.get(urlEqualTo("/userservice/api/v1/users/999"))
            .willReturn(aResponse()
                    .withStatus(404)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"message\":\"User not found\"}")));

    wireMockExtension.stubFor(WireMock.get(urlPathEqualTo("/userservice/api/v1/users"))
            .withQueryParam("email", equalTo("nonexistent@example.com"))
            .willReturn(aResponse()
                    .withStatus(404)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"message\":\"User not found\"}")));
    wireMockExtension.stubFor(WireMock.get(urlEqualTo("/userservice/api/v1/users/999"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"id\": 999, \"name\": \"Ghost\", \"email\": \"ghost@example.com\"}")));
  }

  private void createTestOrders() {
    savedOrder = Orders.builder()
            .userId(regularUserId)
            .status(OrderStatus.PENDING)
            .totalPrice(new BigDecimal("2029.97"))
            .orderItems(new ArrayList<>())
            .build();
    savedOrder.setCreatedAt(now);
    savedOrder.setUpdatedAt(now);
    savedOrder = ordersRepository.save(savedOrder);

    savedOrder2 = Orders.builder()
            .userId(otherUserId)
            .status(OrderStatus.APPROVED)
            .totalPrice(new BigDecimal("1999.98"))
            .orderItems(new ArrayList<>())
            .build();
    savedOrder2.setCreatedAt(now.minusDays(1));
    savedOrder2.setUpdatedAt(now.minusDays(1));
    savedOrder2 = ordersRepository.save(savedOrder2);
  }

  @AfterEach
  void tearDown() {
    ordersRepository.deleteAll();
    itemsRepository.deleteAll();
  }

  @Nested
  @DisplayName("Create Order Tests")
  class CreateOrderTests {

    @Test
    @DisplayName("Should create order successfully")
    void shouldCreateOrderSuccessfully() {
      OrderResponseDto result = orderFacade.createOrder(orderRequest);

      assertNotNull(result);
      assertNotNull(result.getId());
      assertEquals(regularUserId, result.getUser().getId());
      assertEquals("john.doe@example.com", result.getUser().getEmail());
      assertEquals(OrderStatus.PENDING, result.getStatus());
      assertEquals(new BigDecimal("2029.97"), result.getTotalPrice());

      Orders newOrder = ordersRepository.findById(result.getId()).orElse(null);
      assertNotNull(newOrder);
      assertEquals(regularUserId, newOrder.getUserId());
      assertEquals(OrderStatus.PENDING, newOrder.getStatus());

      wireMockExtension.verify(getRequestedFor(urlPathEqualTo("/userservice/api/v1/users"))
              .withQueryParam("email", equalTo("john.doe@example.com")));
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
      OrderRequestDto invalidRequest = OrderRequestDto.builder()
              .email("nonexistent@example.com")
              .items(Arrays.asList(itemRequest1, itemRequest2))
              .build();

      assertThrows(Exception.class, () -> orderFacade.createOrder(invalidRequest));
    }
  }

  @Nested
  @DisplayName("Get Order By ID Tests")
  class GetOrderByIdTests {

    @Test
    @DisplayName("Should get order by id successfully")
    void shouldGetOrderByIdSuccessfully() {
      OrderResponseDto result = orderFacade.getOrderById(savedOrder.getId());

      assertNotNull(result);
      assertEquals(savedOrder.getId(), result.getId());
      assertEquals(regularUserId, result.getUser().getId());
      assertEquals("john.doe@example.com", result.getUser().getEmail());
      assertEquals(OrderStatus.PENDING, result.getStatus());

      wireMockExtension.verify(getRequestedFor(urlEqualTo("/userservice/api/v1/users/100")));
    }

    @Test
    @DisplayName("Should throw exception when order not found")
    void shouldThrowExceptionWhenOrderNotFound() {
      assertThrows(Exception.class, () -> orderFacade.getOrderById(999L));
    }
  }

  @Nested
  @DisplayName("Get Orders By User ID Tests")
  class GetOrdersByUserIdTests {

    @Test
    @DisplayName("Should get orders by user id successfully")
    void shouldGetOrdersByUserIdSuccessfully() {
      UserOrdersListResponseDto result = orderFacade.getOrdersByUserId(regularUserId);

      assertNotNull(result);
      assertNotNull(result.getUser());
      assertEquals(regularUserId, result.getUser().getId());
      assertEquals("john.doe@example.com", result.getUser().getEmail());
      assertEquals(1, result.getOrders().size());
      assertEquals(savedOrder.getId(), result.getOrders().get(0).getId());

      wireMockExtension.verify(getRequestedFor(urlEqualTo("/userservice/api/v1/users/100")));
    }
    @Test
    @DisplayName("Should return empty list when user has no orders")
    void shouldReturnEmptyListWhenUserHasNoOrders() {
      UserOrdersListResponseDto result = orderFacade.getOrdersByUserId(999L);

      assertNotNull(result);
      assertNotNull(result.getUser());
      assertEquals(999L, result.getUser().getId());
      assertTrue(result.getOrders().isEmpty());
    }

    @Test
    @DisplayName("Should handle multiple orders for same user")
    void shouldHandleMultipleOrdersForSameUser() {
      Orders anotherOrder = Orders.builder()
              .userId(regularUserId)
              .status(OrderStatus.DELIVERED)
              .totalPrice(new BigDecimal("59.98"))
              .build();
      anotherOrder.setCreatedAt(now);
      anotherOrder.setUpdatedAt(now);
      ordersRepository.save(anotherOrder);

      UserOrdersListResponseDto result = orderFacade.getOrdersByUserId(regularUserId);

      assertNotNull(result);
      assertEquals(2, result.getOrders().size());
    }
  }

  @Nested
  @DisplayName("Update Order Tests")
  class UpdateOrderTests {

    @Test
    @DisplayName("Should update order successfully")
    void shouldUpdateOrderSuccessfully() {
      OrderResponseDto result = orderFacade.updateOrderById(savedOrder.getId(), orderUpdateRequest);

      assertNotNull(result);
      assertEquals(savedOrder.getId(), result.getId());
      assertEquals(regularUserId, result.getUser().getId());
      assertEquals(OrderStatus.APPROVED, result.getStatus());

      Orders updatedOrder = ordersRepository.findById(savedOrder.getId()).orElse(null);
      assertNotNull(updatedOrder);
      assertEquals(OrderStatus.APPROVED, updatedOrder.getStatus());

      wireMockExtension.verify(1, getRequestedFor(urlPathEqualTo("/userservice/api/v1/users"))
              .withQueryParam("email", equalTo("john.doe@example.com")));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent order")
    void shouldThrowExceptionWhenUpdatingNonExistentOrder() {
      assertThrows(Exception.class,
              () -> orderFacade.updateOrderById(999L, orderUpdateRequest));
    }
  }

  @Nested
  @DisplayName("Delete Order Tests")
  class DeleteOrderTests {

    @Test
    @DisplayName("Should delete order successfully")
    void shouldDeleteOrderSuccessfully() {
      orderFacade.deleteOrderById(savedOrder.getId());

      assertThrows(Exception.class, () -> orderFacade.getOrderById(savedOrder.getId()));

      assertFalse(ordersRepository.existsById(savedOrder.getId()));
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent order")
    void shouldThrowExceptionWhenDeletingNonExistentOrder() {
      assertThrows(Exception.class, () -> orderFacade.deleteOrderById(999L));
    }
  }

  @Nested
  @DisplayName("Find All Orders Tests")
  class FindAllOrdersTests {

    @Test
    @DisplayName("Should find all orders with pagination")
    void shouldFindAllOrdersWithPagination() {
      PageResponseDto<OrderResponseDto> result = orderFacade.findAllOrders(
              OrderSearchCriteriaDto.builder().build(),
              pageable
      );

      assertNotNull(result);
      assertEquals(2, result.getTotalElements());
      assertEquals(2, result.getContent().size());
      assertEquals(0, result.getCurrentPage());
      assertEquals(10, result.getPageSize());

      wireMockExtension.verify(getRequestedFor(urlPathEqualTo("/userservice/api/v1/users/batch"))
              .withQueryParam("ids", equalTo("100"))
              .withQueryParam("ids", equalTo("200")));
    }

    @Test
    @DisplayName("Should filter orders by status")
    void shouldFilterOrdersByStatus() {
      OrderSearchCriteriaDto criteria = OrderSearchCriteriaDto.builder()
              .status(String.valueOf(OrderStatus.PENDING))
              .build();

      PageResponseDto<OrderResponseDto> result = orderFacade.findAllOrders(criteria, pageable);

      assertNotNull(result);
      assertEquals(1, result.getTotalElements());
      assertEquals(OrderStatus.PENDING, result.getContent().get(0).getStatus());
    }

    @Test
    @DisplayName("Should filter orders by date range")
    void shouldFilterOrdersByDateRange() {
      OrderSearchCriteriaDto criteria = OrderSearchCriteriaDto.builder()
              .fromDate(now.minusHours(1))
              .toDate(now.plusDays(1))
              .build();

      PageResponseDto<OrderResponseDto> result = orderFacade.findAllOrders(criteria, pageable);

      assertEquals(2, result.getTotalElements());
      assertEquals(savedOrder.getId(), result.getContent().get(0).getId());
    }

    @Test
    @DisplayName("Should handle empty page")
    void shouldHandleEmptyPage() {
      ordersRepository.deleteAll();

      PageResponseDto<OrderResponseDto> result = orderFacade.findAllOrders(
              OrderSearchCriteriaDto.builder().build(),
              pageable
      );

      assertNotNull(result);
      assertEquals(0, result.getTotalElements());
      assertTrue(result.getContent().isEmpty());
    }
  }

  @Nested
  @DisplayName("Circuit Breaker Tests")
  class CircuitBreakerTests {

    @Test
    @DisplayName("Should handle User Service being unavailable")
    void shouldHandleUserServiceUnavailable() {
      wireMockExtension.stubFor(WireMock.get(urlPathEqualTo("/userservice/api/v1/users"))
              .withQueryParam("email", equalTo("john.doe@example.com"))
              .willReturn(aResponse()
                      .withStatus(503)
                      .withHeader("Content-Type", "application/json")));

      assertThrows(Exception.class, () -> orderFacade.createOrder(orderRequest));
    }

    @Test
    @DisplayName("Should handle timeout from User Service")
    void shouldHandleTimeout() {
      wireMockExtension.stubFor(WireMock.get(urlPathEqualTo("/userservice/api/v1/users"))
              .withQueryParam("email", equalTo("john.doe@example.com"))
              .willReturn(aResponse()
                      .withFixedDelay(5000)
                      .withStatus(200)
                      .withHeader("Content-Type", "application/json")
                      .withBody("{}")));

      assertThrows(Exception.class, () -> orderFacade.createOrder(orderRequest));
    }
  }
}