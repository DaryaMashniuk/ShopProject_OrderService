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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
import com.innowise.orderservice.model.events.OrderCreatedEvent;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.awaitility.Awaitility.await;

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

  @MockitoBean(name = "authorisationService")
  private AuthorisationService authorisationService;

  @Value("${kafka.order.topic.name}")
  private String orderTopicName;

  private OrderItemRequestDto item1;
  private OrderItemRequestDto item2;
  private OrderRequestDto orderRequest;
  private OrderUpdateDto orderUpdateRequest;
  private Orders savedOrder;
  private Orders savedOrder2;
  private Orders savedOrder3;
  private Orders deletedOrder;
  private Long regularUserId = 100L;
  private Long otherUserId = 200L;
  private Long thirdUserId = 300L;
  private Long realId1;
  private Long realId2;
  private LocalDateTime now;
  private DateTimeFormatter dateTimeFormatter;

  private Consumer<String, OrderCreatedEvent> kafkaConsumer;

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
    dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;
    when(authorisationService.hasAdminRole(any())).thenReturn(false);
    when(authorisationService.isSelf(eq(regularUserId), any())).thenReturn(true);
    when(authorisationService.isSelf(eq(otherUserId), any())).thenReturn(false);
    when(authorisationService.isSelf(eq(thirdUserId), any())).thenReturn(false);
    when(authorisationService.isSelf(eq(999L), any())).thenReturn(false);

    Items itemEntity1 = Items.builder()
            .name("Laptop")
            .price(new BigDecimal("1000.00"))
            .build();
    Items itemEntity2 = Items.builder()
            .name("Mouse")
            .price(new BigDecimal("50.00"))
            .build();

    List<Items> savedItems = itemsRepository.saveAll(Arrays.asList(itemEntity1, itemEntity2));
    realId1 = savedItems.get(0).getId();
    realId2 = savedItems.get(1).getId();
    item1 = OrderItemRequestDto.builder()
            .itemId(realId1)
            .quantity(2)
            .build();

    item2 = OrderItemRequestDto.builder()
            .itemId(realId2)
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

    createTestOrders();

    setupUserServiceStubs();

    setupKafkaConsumer();
  }

  private void setupKafkaConsumer() {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-" + System.currentTimeMillis());
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
    props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
    props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, OrderCreatedEvent.class.getName());

    DefaultKafkaConsumerFactory<String, OrderCreatedEvent> factory =
            new DefaultKafkaConsumerFactory<>(props);
    kafkaConsumer = factory.createConsumer();
    kafkaConsumer.subscribe(Collections.singletonList(orderTopicName));
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

    wireMockExtension.stubFor(WireMock.get(urlPathEqualTo("/userservice/api/v1/users"))
            .withQueryParam("email", equalTo("bob.wilson@example.com"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("""
                            {
                                "content": [{
                                    "id": 300,
                                    "name": "Bob",
                                    "surname": "Wilson",
                                    "email": "bob.wilson@example.com"
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

    wireMockExtension.stubFor(WireMock.get(urlEqualTo("/userservice/api/v1/users/300"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("""
                            {
                                "id": 300,
                                "name": "Bob",
                                "surname": "Wilson",
                                "email": "bob.wilson@example.com"
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
                                },
                                {
                                    "id": 300,
                                    "name": "Bob",
                                    "surname": "Wilson",
                                    "email": "bob.wilson@example.com"
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
  }

  private void createTestOrders() {
    savedOrder = Orders.builder()
            .userId(regularUserId)
            .status(OrderStatus.PENDING)
            .totalPrice(new BigDecimal("2050.00"))
            .build();
    savedOrder.setCreatedAt(now.minusHours(2));
    savedOrder.setUpdatedAt(now.minusHours(2));
    savedOrder = ordersRepository.save(savedOrder);
    savedOrder2 = Orders.builder()
            .userId(otherUserId)
            .status(OrderStatus.APPROVED)
            .totalPrice(new BigDecimal("1999.98"))
            .build();
    savedOrder2.setCreatedAt(now.minusDays(1));
    savedOrder2.setUpdatedAt(now.minusDays(1));
    savedOrder2 = ordersRepository.save(savedOrder2);

    savedOrder3 = Orders.builder()
            .userId(thirdUserId)
            .status(OrderStatus.APPROVED)
            .totalPrice(new BigDecimal("3500.00"))
            .build();
    savedOrder3.setCreatedAt(now.plusHours(2));
    savedOrder3.setUpdatedAt(now.plusHours(2));
    savedOrder3 = ordersRepository.save(savedOrder3);
    deletedOrder = Orders.builder()
            .userId(regularUserId)
            .status(OrderStatus.FAILED)
            .totalPrice(new BigDecimal("500.00"))
            .deleted(true)
            .build();
    deletedOrder.setCreatedAt(now.minusDays(5));
    deletedOrder.setUpdatedAt(now.minusDays(5));
    deletedOrder = ordersRepository.save(deletedOrder);
  }

  @AfterEach
  void tearDown() {
    if (kafkaConsumer != null) {
      kafkaConsumer.close();
    }
    ordersRepository.deleteAll();
    itemsRepository.deleteAll();
  }

  @Nested
  @DisplayName("Create Order Tests")
  class CreateOrderTests {


    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should create order with single item successfully")
    void shouldCreateOrderWithSingleItem() throws Exception {
      when(authorisationService.hasAdminRole(any())).thenReturn(true);

      OrderRequestDto singleItemRequest = OrderRequestDto.builder()
              .email("john.doe@example.com")
              .items(Collections.singletonList(item1))
              .build();

      mockMvc.perform(post("/api/v1/orders")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(singleItemRequest)))
              .andExpect(status().isCreated())
              .andExpect(jsonPath("$.totalPrice").value(2000.0));
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
      ConsumerRecords<String, OrderCreatedEvent> records =
              KafkaTestUtils.getRecords(kafkaConsumer, Duration.ofSeconds(1));
      assertThat(records.count()).isEqualTo(0);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 400 when order has no items")
    void shouldReturn400WhenOrderHasNoItems() throws Exception {
      when(authorisationService.hasAdminRole(any())).thenReturn(true);

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
      when(authorisationService.hasAdminRole(any())).thenReturn(true);

      OrderRequestDto invalidRequest = OrderRequestDto.builder()
              .email("invalid-email")
              .items(Arrays.asList(item1, item2))
              .build();

      mockMvc.perform(post("/api/v1/orders")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(invalidRequest)))
              .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 404 when user not found")
    void shouldReturn404WhenUserNotFound() throws Exception {
      when(authorisationService.hasAdminRole(any())).thenReturn(true);

      OrderRequestDto invalidRequest = OrderRequestDto.builder()
              .email("nonexistent@example.com")
              .items(Arrays.asList(item1, item2))
              .build();

      mockMvc.perform(post("/api/v1/orders")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(invalidRequest)))
              .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("Get Order By ID Tests")
  class GetOrderByIdTests {

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should get order by id successfully with admin role")
    void shouldGetOrderByIdSuccessfullyWithAdmin() throws Exception {
      when(authorisationService.hasAdminRole(any())).thenReturn(true);

      mockMvc.perform(get("/api/v1/orders/{id}", savedOrder.getId()))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.id").value(savedOrder.getId()))
              .andExpect(jsonPath("$.user.id").value(regularUserId))
              .andExpect(jsonPath("$.user.name").value("John"))
              .andExpect(jsonPath("$.user.surname").value("Doe"))
              .andExpect(jsonPath("$.user.email").value("john.doe@example.com"))
              .andExpect(jsonPath("$.status").value("PENDING"))
              .andExpect(jsonPath("$.totalPrice").value(2050.0));

      wireMockExtension.verify(getRequestedFor(urlEqualTo("/userservice/api/v1/users/100")));
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
      when(authorisationService.hasAdminRole(any())).thenReturn(true);

      mockMvc.perform(get("/api/v1/orders/{id}", 999L))
              .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 404 when order is deleted")
    void shouldReturn404WhenOrderIsDeleted() throws Exception {
      when(authorisationService.hasAdminRole(any())).thenReturn(true);

      mockMvc.perform(get("/api/v1/orders/{id}", deletedOrder.getId()))
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
      when(authorisationService.hasAdminRole(any())).thenReturn(true);

      mockMvc.perform(get("/api/v1/orders/users/{userId}", regularUserId))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.user.id").value(regularUserId))
              .andExpect(jsonPath("$.user.name").value("John"))
              .andExpect(jsonPath("$.user.email").value("john.doe@example.com"))
              .andExpect(jsonPath("$.orders", hasSize(1)))
              .andExpect(jsonPath("$.orders[0].id").value(savedOrder.getId()))
              .andExpect(jsonPath("$.orders[0].status").value("PENDING"));
    }

    @Test
    @DisplayName("Should get own orders by user id successfully")
    void shouldGetOwnOrdersByUserIdSuccessfully() throws Exception {
      when(authorisationService.hasAdminRole(any())).thenReturn(false);
      when(authorisationService.isSelf(eq(regularUserId), any())).thenReturn(true);

      mockMvc.perform(get("/api/v1/orders/users/{userId}", regularUserId)
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

      mockMvc.perform(get("/api/v1/orders/users/{userId}", otherUserId)
                      .with(user(String.valueOf(regularUserId)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
              .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should not return deleted orders")
    void shouldNotReturnDeletedOrders() throws Exception {
      when(authorisationService.hasAdminRole(any())).thenReturn(true);

      mockMvc.perform(get("/api/v1/orders/users/{userId}", regularUserId))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.orders", hasSize(1)))
              .andExpect(jsonPath("$.orders[0].id").value(savedOrder.getId()));
    }
  }

  @Nested
  @DisplayName("Update Order Tests")
  class UpdateOrderTests {

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should update order successfully with admin role")
    void shouldUpdateOrderSuccessfullyWithAdmin() throws Exception {
      when(authorisationService.hasAdminRole(any())).thenReturn(true);

      mockMvc.perform(patch("/api/v1/orders/{id}", savedOrder.getId())
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(orderUpdateRequest)))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.id").value(savedOrder.getId()))
              .andExpect(jsonPath("$.user.id").value(regularUserId))
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
      when(authorisationService.hasAdminRole(any())).thenReturn(true);

      mockMvc.perform(patch("/api/v1/orders/{id}", 999L)
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(orderUpdateRequest)))
              .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 404 when updating deleted order")
    void shouldReturn404WhenUpdatingDeletedOrder() throws Exception {
      when(authorisationService.hasAdminRole(any())).thenReturn(true);

      mockMvc.perform(patch("/api/v1/orders/{id}", deletedOrder.getId())
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(orderUpdateRequest)))
              .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 404 when updating with non-existent user email")
    void shouldReturn404WhenUserEmailNotFound() throws Exception {
      when(authorisationService.hasAdminRole(any())).thenReturn(true);

      OrderUpdateDto invalidUpdate = OrderUpdateDto.builder()
              .email("nonexistent@example.com")
              .status("APPROVED")
              .items(Collections.singletonList(item1))
              .build();

      mockMvc.perform(patch("/api/v1/orders/{id}", savedOrder.getId())
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(invalidUpdate)))
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
      when(authorisationService.hasAdminRole(any())).thenReturn(true);

      mockMvc.perform(delete("/api/v1/orders/{id}", savedOrder.getId()))
              .andExpect(status().isNoContent());
      mockMvc.perform(get("/api/v1/orders/{id}", savedOrder.getId())
                      .with(user(String.valueOf(regularUserId)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
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
      when(authorisationService.hasAdminRole(any())).thenReturn(true);

      mockMvc.perform(delete("/api/v1/orders/{id}", 999L))
              .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 404 when deleting already deleted order")
    void shouldReturn404WhenDeletingAlreadyDeletedOrder() throws Exception {
      when(authorisationService.hasAdminRole(any())).thenReturn(true);

      mockMvc.perform(delete("/api/v1/orders/{id}", deletedOrder.getId()))
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
      when(authorisationService.hasAdminRole(any())).thenReturn(true);

      mockMvc.perform(get("/api/v1/orders")
                      .param("page", "0")
                      .param("size", "10"))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.content").isArray())
              .andExpect(jsonPath("$.content", hasSize(3)))
              .andExpect(jsonPath("$.currentPage").value(0))
              .andExpect(jsonPath("$.pageSize").value(10))
              .andExpect(jsonPath("$.totalElements").value(3))
              .andExpect(jsonPath("$.totalPages").value(1))
              .andExpect(jsonPath("$.first").value(true))
              .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should filter orders by status")
    void shouldFilterOrdersByStatus() throws Exception {
      when(authorisationService.hasAdminRole(any())).thenReturn(true);

      mockMvc.perform(get("/api/v1/orders")
                      .param("status", "PENDING")
                      .param("page", "0")
                      .param("size", "10"))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.content", hasSize(1)))
              .andExpect(jsonPath("$.content[0].status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should filter orders by combination of criteria")
    void shouldFilterOrdersByCombination() throws Exception {
      when(authorisationService.hasAdminRole(any())).thenReturn(true);

      mockMvc.perform(get("/api/v1/orders")
                      .param("status", "PENDING")
                      .param("fromDate", now.minusHours(1).format(dateTimeFormatter))
                      .param("toDate", now.plusHours(1).format(dateTimeFormatter))
                      .param("page", "0")
                      .param("size", "10"))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.content", hasSize(1)))
              .andExpect(jsonPath("$.content[0].id").value(savedOrder.getId()))
              .andExpect(jsonPath("$.content[0].status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return empty page when no orders match filters")
    void shouldReturnEmptyPageWhenNoOrdersMatchFilters() throws Exception {
      when(authorisationService.hasAdminRole(any())).thenReturn(true);

      mockMvc.perform(get("/api/v1/orders")
                      .param("status", "DELIVERED")
                      .param("page", "0")
                      .param("size", "10"))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.content", hasSize(0)))
              .andExpect(jsonPath("$.totalElements").value(0));
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
      when(authorisationService.hasAdminRole(any())).thenReturn(true);

      OrderItemRequestDto invalidItem = OrderItemRequestDto.builder()
              .itemId(realId1)
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
      when(authorisationService.hasAdminRole(any())).thenReturn(true);

      OrderItemRequestDto invalidItem = OrderItemRequestDto.builder()
              .itemId(realId1)
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

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 400 when item ID is null")
    void shouldReturn400WhenItemIdIsNull() throws Exception {
      when(authorisationService.hasAdminRole(any())).thenReturn(true);

      OrderItemRequestDto invalidItem = OrderItemRequestDto.builder()
              .itemId(null)
              .quantity(1)
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

  @Nested
  @DisplayName("Pagination Tests")
  class PaginationTests {

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should handle page size parameter")
    void shouldHandlePageSize() throws Exception {
      when(authorisationService.hasAdminRole(any())).thenReturn(true);

      mockMvc.perform(get("/api/v1/orders")
                      .param("page", "0")
                      .param("size", "2"))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.content", hasSize(2)))
              .andExpect(jsonPath("$.pageSize").value(2))
              .andExpect(jsonPath("$.totalElements").value(3))
              .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should handle second page")
    void shouldHandleSecondPage() throws Exception {
      when(authorisationService.hasAdminRole(any())).thenReturn(true);

      mockMvc.perform(get("/api/v1/orders")
                      .param("page", "1")
                      .param("size", "2"))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.content", hasSize(1)))
              .andExpect(jsonPath("$.currentPage").value(1))
              .andExpect(jsonPath("$.totalPages").value(2));
    }


    @Nested
    @DisplayName("Circuit Breaker Tests")
    class CircuitBreakerTests {

      @Test
      @WithMockUser(roles = "ADMIN")
      @DisplayName("Should handle User Service being unavailable")
      void shouldHandleUserServiceUnavailable() throws Exception {
        when(authorisationService.hasAdminRole(any())).thenReturn(true);

        wireMockExtension.stubFor(WireMock.get(urlPathEqualTo("/userservice/api/v1/users"))
                .withQueryParam("email", equalTo("john.doe@example.com"))
                .willReturn(aResponse()
                        .withStatus(503)));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isServiceUnavailable());
      }

      @Test
      @WithMockUser(roles = "ADMIN")
      @DisplayName("Should handle User Service timeout")
      void shouldHandleUserServiceTimeout() throws Exception {
        when(authorisationService.hasAdminRole(any())).thenReturn(true);

        wireMockExtension.stubFor(WireMock.get(urlPathEqualTo("/userservice/api/v1/users"))
                .withQueryParam("email", equalTo("john.doe@example.com"))
                .willReturn(aResponse()
                        .withFixedDelay(5000)
                        .withStatus(200)
                        .withBody("{}")));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isServiceUnavailable());
      }
    }
  }
}