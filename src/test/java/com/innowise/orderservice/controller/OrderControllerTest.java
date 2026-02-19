package com.innowise.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.innowise.orderservice.BaseIntegrationTest;
import com.innowise.orderservice.model.Items;
import com.innowise.orderservice.model.OrderStatus;
import com.innowise.orderservice.model.Orders;
import com.innowise.orderservice.model.dto.request.OrderItemRequestDto;
import com.innowise.orderservice.model.dto.request.OrderRequestDto;
import com.innowise.orderservice.model.dto.request.OrderUpdateDto;
import com.innowise.orderservice.repository.ItemsRepository;
import com.innowise.orderservice.repository.OrdersRepository;
import com.innowise.orderservice.service.AuthorisationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OrderControllerTest extends BaseIntegrationTest {

  @RegisterExtension
  public static WireMockExtension wireMockExtension = WireMockExtension.newInstance()
          .options(wireMockConfig().port(8081))
          .build();

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private OrdersRepository ordersRepository;

  @Autowired
  private ItemsRepository itemsRepository;

  @Mock(name = "authorisationService")
  private AuthorisationService authorisationService;


  private OrderItemRequestDto item1;
  private OrderItemRequestDto item2;
  private OrderRequestDto orderRequest;
  private OrderUpdateDto orderUpdateRequest;
  private Orders savedOrder;
  private Orders savedOrder2;
  private Long regularUserId = 100L;
  private Long otherUserId = 200L;
  private Long realId1;

  @Override
  protected WireMockExtension getWireMockExtension() {
    return wireMockExtension;
  }

  @BeforeEach
  void setUp() {
    ordersRepository.deleteAll();

    wireMockExtension.resetAll();

    when(authorisationService.hasAdminRole(any())).thenReturn(false);
    when(authorisationService.isSelf(eq(regularUserId), any())).thenReturn(true);
    when(authorisationService.isSelf(eq(otherUserId), any())).thenReturn(false);
    when(authorisationService.isSelf(eq(999L), any())).thenReturn(false);

    item1 = OrderItemRequestDto.builder()
            .itemId(1L)
            .quantity(2)
            .build();

    item2 = OrderItemRequestDto.builder()
            .itemId(2L)
            .quantity(1)
            .build();

    orderRequest = OrderRequestDto.builder()
            .email("john.doe@example.com")
            .items(Arrays.asList(item1, item2))
            .build();

    List<OrderItemRequestDto> updateItems = Collections.singletonList(
            OrderItemRequestDto.builder()
                    .itemId(realId1)
                    .quantity(3)
                    .build()
    );

    orderUpdateRequest = OrderUpdateDto.builder()
            .email("john.doe@example.com")
            .status("APPROVED")
            .items(updateItems)
            .build();

    setupUserServiceStubs();

    createTestOrders();
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
  }

  private void createTestOrders() {
    Items itemEntity1 = Items.builder()
            .name("Item 1")
            .price(new BigDecimal("100.00"))
            .build();

    Items itemEntity2 = Items.builder()
            .name("Item 2")
            .price(new BigDecimal("200.00"))
            .build();

    List<Items> savedItems = itemsRepository.saveAll(Arrays.asList(itemEntity1, itemEntity2));
    realId1 = savedItems.get(0).getId();
    Long realId2 = savedItems.get(1).getId();

    this.item1 = OrderItemRequestDto.builder()
            .itemId(realId1)
            .quantity(2)
            .build();

    this.item2 = OrderItemRequestDto.builder()
            .itemId(realId2)
            .quantity(1)
            .build();

    itemsRepository.saveAll(Arrays.asList(itemEntity1, itemEntity2));
    savedOrder = Orders.builder()
            .userId(regularUserId)
            .status(OrderStatus.PENDING)
            .totalPrice(new BigDecimal("2029.97"))
            .build();
    savedOrder.setCreatedAt(LocalDateTime.now());
    savedOrder.setUpdatedAt(LocalDateTime.now());
    savedOrder = ordersRepository.save(savedOrder);

    this.orderRequest = OrderRequestDto.builder()
            .email("john.doe@example.com")
            .items(Arrays.asList(item1, item2))
            .build();
    savedOrder2 = Orders.builder()
            .userId(otherUserId)
            .status(OrderStatus.APPROVED)
            .totalPrice(new BigDecimal("1999.98"))
            .build();
    savedOrder2.setCreatedAt(LocalDateTime.now().minusDays(1));
    savedOrder2.setUpdatedAt(LocalDateTime.now().minusDays(1));
    savedOrder2 = ordersRepository.save(savedOrder2);
  }

  @AfterEach
  void tearDown() {
    ordersRepository.deleteAll();
  }

  @Nested
  @DisplayName("Create Order Tests")
  class CreateOrderTests {

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should create order successfully with admin role")
    void shouldCreateOrderSuccessfullyWithAdmin() throws Exception {
      mockMvc.perform(post("/api/v1/orders")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(orderRequest)))
              .andExpect(status().isCreated())
              .andExpect(jsonPath("$.id").isNumber())
              .andExpect(jsonPath("$.user.id").value(regularUserId))
              .andExpect(jsonPath("$.user.email").value("john.doe@example.com"))
              .andExpect(jsonPath("$.status").value("PENDING"))
              .andExpect(jsonPath("$.totalPrice").value(400.0));
    }

    @Test
    @DisplayName("Should return 403 when regular user tries to create order")
    void shouldReturnForbiddenWhenRegularUserCreatesOrder() throws Exception {
      when(authorisationService.hasAdminRole(any())).thenReturn(false);

      mockMvc.perform(post("/api/v1/orders")
                      .with(user(String.valueOf(regularUserId)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(orderRequest)))
              .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 400 when order has no items")
    void shouldReturn400WhenOrderHasNoItems() throws Exception {
      OrderRequestDto invalidRequest = OrderRequestDto.builder()
              .email("john.doe@example.com")
              .items(Collections.emptyList())
              .build();

      mockMvc.perform(post("/api/v1/orders")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(invalidRequest)))
              .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 400 when email is invalid")
    void shouldReturn400WhenEmailInvalid() throws Exception {
      OrderRequestDto invalidRequest = OrderRequestDto.builder()
              .email("invalid-email")
              .items(Arrays.asList(item1, item2))
              .build();

      mockMvc.perform(post("/api/v1/orders")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(invalidRequest)))
              .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("Get Order By ID Tests")
  class GetOrderByIdTests {

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should get order by id successfully with admin role")
    void shouldGetOrderByIdSuccessfullyWithAdmin() throws Exception {
      mockMvc.perform(get("/api/v1/orders/{id}", savedOrder.getId()))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.id").value(savedOrder.getId()))
              .andExpect(jsonPath("$.user.id").value(regularUserId))
              .andExpect(jsonPath("$.user.email").value("john.doe@example.com"))
              .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("Should return 403 when getting other user's order")
    void shouldReturnForbiddenWhenGettingOtherUserOrder() throws Exception {
      when(authorisationService.hasAdminRole(any())).thenReturn(false);
      when(authorisationService.isSelf(eq(regularUserId), any())).thenReturn(false);

      mockMvc.perform(get("/api/v1/orders/{id}", savedOrder.getId())
                      .with(user(String.valueOf(otherUserId)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
              .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 404 when order not found")
    void shouldReturn404WhenOrderNotFound() throws Exception {
      mockMvc.perform(get("/api/v1/orders/{id}", 999L))
              .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("Get Orders By User ID Tests")
  class GetOrdersByUserIdTests {

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should get orders by user id successfully with admin role")
    void shouldGetOrdersByUserIdSuccessfullyWithAdmin() throws Exception {
      mockMvc.perform(get("/api/v1/orders/user/{userId}", regularUserId))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.user.id").value(regularUserId))
              .andExpect(jsonPath("$.user.email").value("john.doe@example.com"))
              .andExpect(jsonPath("$.orders", hasSize(1)))
              .andExpect(jsonPath("$.orders[0].id").value(savedOrder.getId()));
    }

    @Test
    @DisplayName("Should get own orders by user id successfully")
    void shouldGetOwnOrdersByUserIdSuccessfully() throws Exception {
      when(authorisationService.hasAdminRole(any())).thenReturn(false);
      when(authorisationService.isSelf(eq(regularUserId), any())).thenReturn(true);

      mockMvc.perform(get("/api/v1/orders/user/{userId}", regularUserId)
                      .with(user(String.valueOf(regularUserId)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.user.id").value(regularUserId))
              .andExpect(jsonPath("$.orders", hasSize(1)));
    }

    @Test
    @DisplayName("Should return 403 when getting other user's orders")
    void shouldReturnForbiddenWhenGettingOtherUserOrders() throws Exception {
      when(authorisationService.hasAdminRole(any())).thenReturn(false);
      when(authorisationService.isSelf(eq(otherUserId), any())).thenReturn(false);

      mockMvc.perform(get("/api/v1/orders/user/{userId}", otherUserId)
                      .with(user(String.valueOf(regularUserId)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
              .andExpect(status().isForbidden());
    }
  }

  @Nested
  @DisplayName("Update Order Tests")
  class UpdateOrderTests {

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should update order successfully with admin role")
    void shouldUpdateOrderSuccessfullyWithAdmin() throws Exception {
      mockMvc.perform(patch("/api/v1/orders/{id}", savedOrder.getId())
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(orderUpdateRequest)))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.id").value(savedOrder.getId()))
              .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @DisplayName("Should return 403 when regular user tries to update order")
    void shouldReturnForbiddenWhenRegularUserUpdatesOrder() throws Exception {
      when(authorisationService.hasAdminRole(any())).thenReturn(false);

      mockMvc.perform(patch("/api/v1/orders/{id}", savedOrder.getId())
                      .with(user(String.valueOf(regularUserId)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(orderUpdateRequest)))
              .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 404 when updating non-existent order")
    void shouldReturn404WhenUpdatingNonExistentOrder() throws Exception {
      mockMvc.perform(patch("/api/v1/orders/{id}", 999L)
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(orderUpdateRequest)))
              .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("Delete Order Tests")
  class DeleteOrderTests {

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should delete order successfully with admin role")
    void shouldDeleteOrderSuccessfullyWithAdmin() throws Exception {
      mockMvc.perform(delete("/api/v1/orders/{id}", savedOrder.getId()))
              .andExpect(status().isNoContent());

      // Verify order is deleted
      mockMvc.perform(get("/api/v1/orders/{id}", savedOrder.getId()))
              .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 403 when regular user tries to delete order")
    void shouldReturnForbiddenWhenRegularUserDeletesOrder() throws Exception {
      when(authorisationService.hasAdminRole(any())).thenReturn(false);

      mockMvc.perform(delete("/api/v1/orders/{id}", savedOrder.getId())
                      .with(user(String.valueOf(regularUserId)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
              .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 404 when deleting non-existent order")
    void shouldReturn404WhenDeletingNonExistentOrder() throws Exception {
      mockMvc.perform(delete("/api/v1/orders/{id}", 999L))
              .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("Find All Orders Tests")
  class FindAllOrdersTests {

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should find all orders with pagination")
    void shouldFindAllOrdersWithPagination() throws Exception {
      mockMvc.perform(get("/api/v1/orders")
                      .param("page", "0")
                      .param("size", "10"))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.content").isArray())
              .andExpect(jsonPath("$.content", hasSize(2)))
              .andExpect(jsonPath("$.currentPage").value(0))
              .andExpect(jsonPath("$.pageSize").value(10))
              .andExpect(jsonPath("$.totalElements").value(2))
              .andExpect(jsonPath("$.first").value(true))
              .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should filter orders by status")
    void shouldFilterOrdersByStatus() throws Exception {
      mockMvc.perform(get("/api/v1/orders")
                      .param("status", "PENDING")
                      .param("page", "0")
                      .param("size", "10"))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.content").isArray())
              .andExpect(jsonPath("$.content", hasSize(1)))
              .andExpect(jsonPath("$.content[0].status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should filter orders by date range")
    void shouldFilterOrdersByDateRange() throws Exception {
      LocalDateTime now = LocalDateTime.now();

      mockMvc.perform(get("/api/v1/orders")
                      .param("fromDate", now.minusDays(2).toString())
                      .param("toDate", now.plusDays(1).toString())
                      .param("page", "0")
                      .param("size", "10"))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.content").isArray())
              .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    @DisplayName("Should return 403 when regular user tries to get all orders")
    void shouldReturnForbiddenWhenRegularUserGetsAllOrders() throws Exception {
      when(authorisationService.hasAdminRole(any())).thenReturn(false);

      mockMvc.perform(get("/api/v1/orders")
                      .with(user(String.valueOf(regularUserId)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
              .andExpect(status().isForbidden());
    }
  }

  @Nested
  @DisplayName("Validation Tests")
  class ValidationTests {

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 400 when order items contain negative quantity")
    void shouldReturn400WhenNegativeQuantity() throws Exception {
      OrderItemRequestDto invalidItem = OrderItemRequestDto.builder()
              .itemId(1L)
              .quantity(-1)
              .build();

      OrderRequestDto invalidRequest = OrderRequestDto.builder()
              .email("john.doe@example.com")
              .items(Collections.singletonList(invalidItem))
              .build();

      mockMvc.perform(post("/api/v1/orders")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(invalidRequest)))
              .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 400 when order items quantity exceeds max")
    void shouldReturn400WhenQuantityExceedsMax() throws Exception {
      OrderItemRequestDto invalidItem = OrderItemRequestDto.builder()
              .itemId(1L)
              .quantity(10001)
              .build();

      OrderRequestDto invalidRequest = OrderRequestDto.builder()
              .email("john.doe@example.com")
              .items(Collections.singletonList(invalidItem))
              .build();

      mockMvc.perform(post("/api/v1/orders")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(invalidRequest)))
              .andExpect(status().isBadRequest());
    }
  }
}