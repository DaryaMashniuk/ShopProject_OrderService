package com.innowise.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.innowise.orderservice.BaseIntegrationTest;
import com.innowise.orderservice.model.Items;
import com.innowise.orderservice.model.dto.request.ItemsRequestDto;
import com.innowise.orderservice.repository.ItemsRepository;
import com.innowise.orderservice.service.AuthorisationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ItemControllerTest extends BaseIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private ItemsRepository itemsRepository;

  @Mock(name = "authorisationService")
  private AuthorisationService authorisationService;

  private Items testItem;
  private Items testItem2;
  private ItemsRequestDto itemRequestDto;
  private ItemsRequestDto updateRequestDto;
  private Long regularUserId = 100L;

  @Override
  protected WireMockExtension getWireMockExtension() {
    return null;
  }

  @BeforeEach
  void setUp() {
    itemsRepository.deleteAll();

    when(authorisationService.hasAdminRole(any())).thenReturn(false);
    when(authorisationService.isSelf(any(), any())).thenReturn(false);

    testItem = Items.builder()
            .name("Laptop")
            .price(new BigDecimal("999.99"))
            .build();
    testItem = itemsRepository.save(testItem);

    testItem2 = Items.builder()
            .name("Mouse")
            .price(new BigDecimal("29.99"))
            .build();
    testItem2 = itemsRepository.save(testItem2);

    itemRequestDto = ItemsRequestDto.builder()
            .name("Keyboard")
            .price(new BigDecimal("89.99"))
            .build();

    updateRequestDto = ItemsRequestDto.builder()
            .name("Gaming Laptop")
            .price(new BigDecimal("1299.99"))
            .build();
  }

  @AfterEach
  void tearDown() {
    itemsRepository.deleteAll();
  }

  @Nested
  @DisplayName("Create Item Tests")
  class CreateItemTests {

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should create item successfully with admin role")
    void shouldCreateItemSuccessfullyWithAdmin() throws Exception {
      mockMvc.perform(post("/api/v1/items")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(itemRequestDto)))
              .andExpect(status().isCreated())
              .andExpect(jsonPath("$.id").isNumber())
              .andExpect(jsonPath("$.name").value("Keyboard"))
              .andExpect(jsonPath("$.price").value(89.99));
    }

    @Test
    @DisplayName("Should return 403 when regular user tries to create item")
    void shouldReturnForbiddenWhenRegularUserCreatesItem() throws Exception {
      when(authorisationService.hasAdminRole(any())).thenReturn(false);

      mockMvc.perform(post("/api/v1/items")
                      .with(user(String.valueOf(regularUserId)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(itemRequestDto)))
              .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 400 when name is empty")
    void shouldReturn400WhenNameIsEmpty() throws Exception {
      ItemsRequestDto invalidRequest = ItemsRequestDto.builder()
              .name("")
              .price(new BigDecimal("89.99"))
              .build();

      mockMvc.perform(post("/api/v1/items")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(invalidRequest)))
              .andExpect(status().isBadRequest())
              .andExpect(jsonPath("$.details.name").value("Item's name can't be empty"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 400 when price is null")
    void shouldReturn400WhenPriceIsNull() throws Exception {
      ItemsRequestDto invalidRequest = ItemsRequestDto.builder()
              .name("Keyboard")
              .price(null)
              .build();

      mockMvc.perform(post("/api/v1/items")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(invalidRequest)))
              .andExpect(status().isBadRequest())
              .andExpect(jsonPath("$.details.price").value("Item's price can't be empty"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 400 when price is negative")
    void shouldReturn400WhenPriceIsNegative() throws Exception {
      ItemsRequestDto invalidRequest = ItemsRequestDto.builder()
              .name("Keyboard")
              .price(new BigDecimal("-10.00"))
              .build();

      mockMvc.perform(post("/api/v1/items")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(invalidRequest)))
              .andExpect(status().isBadRequest())
              .andExpect(jsonPath("$.details.price").value("Item's price must be greater that 0"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 409 when item with same name already exists")
    void shouldReturn409WhenDuplicateName() throws Exception {
      ItemsRequestDto duplicateRequest = ItemsRequestDto.builder()
              .name("Laptop")
              .price(new BigDecimal("899.99"))
              .build();

      mockMvc.perform(post("/api/v1/items")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(duplicateRequest)))
              .andExpect(status().isConflict())
              .andExpect(jsonPath("$.message").value(containsString("already exists")));
    }
  }

  @Nested
  @DisplayName("Get Item By ID Tests")
  class GetItemByIdTests {

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should get item by id successfully with admin role")
    void shouldGetItemByIdSuccessfullyWithAdmin() throws Exception {
      mockMvc.perform(get("/api/v1/items/{id}", testItem.getId()))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.id").value(testItem.getId()))
              .andExpect(jsonPath("$.name").value(testItem.getName()))
              .andExpect(jsonPath("$.price").value(testItem.getPrice().doubleValue()));
    }

    @Test
    @DisplayName("Should return 403 when regular user tries to get item")
    void shouldReturnForbiddenWhenRegularUserGetsItem() throws Exception {
      when(authorisationService.hasAdminRole(any())).thenReturn(false);

      mockMvc.perform(get("/api/v1/items/{id}", testItem.getId())
                      .with(user(String.valueOf(regularUserId)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
              .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 404 when item not found")
    void shouldReturn404WhenItemNotFound() throws Exception {
      mockMvc.perform(get("/api/v1/items/{id}", 999L))
              .andExpect(status().isNotFound())
              .andExpect(jsonPath("$.message").value(containsString("not found")));
    }
  }

  @Nested
  @DisplayName("Update Item Tests")
  class UpdateItemTests {

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should update item successfully with admin role")
    void shouldUpdateItemSuccessfullyWithAdmin() throws Exception {
      mockMvc.perform(patch("/api/v1/items/{id}", testItem.getId())
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(updateRequestDto)))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.id").value(testItem.getId()))
              .andExpect(jsonPath("$.name").value("Gaming Laptop"))
              .andExpect(jsonPath("$.price").value(1299.99));
    }

    @Test
    @DisplayName("Should return 403 when regular user tries to update item")
    void shouldReturnForbiddenWhenRegularUserUpdatesItem() throws Exception {
      when(authorisationService.hasAdminRole(any())).thenReturn(false);

      mockMvc.perform(patch("/api/v1/items/{id}", testItem.getId())
                      .with(user(String.valueOf(regularUserId)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(updateRequestDto)))
              .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should update only name successfully")
    void shouldUpdateOnlyNameSuccessfully() throws Exception {
      ItemsRequestDto nameOnlyUpdate = ItemsRequestDto.builder()
              .name("New Name Only")
              .price(testItem.getPrice())
              .build();

      mockMvc.perform(patch("/api/v1/items/{id}", testItem.getId())
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(nameOnlyUpdate)))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.id").value(testItem.getId()))
              .andExpect(jsonPath("$.name").value("New Name Only"))
              .andExpect(jsonPath("$.price").value(testItem.getPrice().doubleValue()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should update only price successfully")
    void shouldUpdateOnlyPriceSuccessfully() throws Exception {
      ItemsRequestDto priceOnlyUpdate = ItemsRequestDto.builder()
              .name(testItem.getName())
              .price(new BigDecimal("799.99"))
              .build();

      mockMvc.perform(patch("/api/v1/items/{id}", testItem.getId())
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(priceOnlyUpdate)))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.id").value(testItem.getId()))
              .andExpect(jsonPath("$.name").value(testItem.getName()))
              .andExpect(jsonPath("$.price").value(799.99));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 404 when updating non-existent item")
    void shouldReturn404WhenUpdatingNonExistentItem() throws Exception {
      mockMvc.perform(patch("/api/v1/items/{id}", 999L)
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(updateRequestDto)))
              .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 409 when updating to existing name")
    void shouldReturn409WhenUpdatingToExistingName() throws Exception {
      ItemsRequestDto duplicateNameRequest = ItemsRequestDto.builder()
              .name("Mouse")
              .price(new BigDecimal("39.99"))
              .build();

      mockMvc.perform(patch("/api/v1/items/{id}", testItem.getId())
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(duplicateNameRequest)))
              .andExpect(status().isConflict())
              .andExpect(jsonPath("$.message").value(containsString("already exists")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 400 when updating with empty name")
    void shouldReturn400WhenUpdatingWithEmptyName() throws Exception {
      ItemsRequestDto invalidRequest = ItemsRequestDto.builder()
              .name("")
              .price(new BigDecimal("799.99"))
              .build();

      mockMvc.perform(patch("/api/v1/items/{id}", testItem.getId())
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(invalidRequest)))
              .andExpect(status().isBadRequest())
              .andExpect(jsonPath("$.details.name").value("Item's name can't be empty"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 400 when updating with negative price")
    void shouldReturn400WhenUpdatingWithNegativePrice() throws Exception {
      ItemsRequestDto invalidRequest = ItemsRequestDto.builder()
              .name("Valid Name")
              .price(new BigDecimal("-50.00"))
              .build();

      mockMvc.perform(patch("/api/v1/items/{id}", testItem.getId())
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(invalidRequest)))
              .andExpect(status().isBadRequest())
              .andExpect(jsonPath("$.details.price").value("Item's price must be greater that 0"));
    }
  }

  @Nested
  @DisplayName("Delete Item Tests")
  class DeleteItemTests {

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should delete item successfully with admin role")
    void shouldDeleteItemSuccessfullyWithAdmin() throws Exception {
      mockMvc.perform(delete("/api/v1/items/{id}", testItem.getId()))
              .andExpect(status().isNoContent());

      mockMvc.perform(get("/api/v1/items/{id}", testItem.getId()))
              .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 403 when regular user tries to delete item")
    void shouldReturnForbiddenWhenRegularUserDeletesItem() throws Exception {
      when(authorisationService.hasAdminRole(any())).thenReturn(false);

      mockMvc.perform(delete("/api/v1/items/{id}", testItem.getId())
                      .with(user(String.valueOf(regularUserId)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
              .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 404 when deleting non-existent item")
    void shouldReturn404WhenDeletingNonExistentItem() throws Exception {
      mockMvc.perform(delete("/api/v1/items/{id}", 999L))
              .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should delete multiple items successfully")
    void shouldDeleteMultipleItemsSuccessfully() throws Exception {
      mockMvc.perform(delete("/api/v1/items/{id}", testItem.getId()))
              .andExpect(status().isNoContent());

      mockMvc.perform(delete("/api/v1/items/{id}", testItem2.getId()))
              .andExpect(status().isNoContent());

      mockMvc.perform(get("/api/v1/items/{id}", testItem.getId()))
              .andExpect(status().isNotFound());

      mockMvc.perform(get("/api/v1/items/{id}", testItem2.getId()))
              .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("Get All Items (Implicit) Tests")
  class GetAllItemsTests {

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should get all items when getting by non-existent ID returns empty")
    void shouldHaveMultipleItemsInDatabase() throws Exception {

      mockMvc.perform(get("/api/v1/items/{id}", testItem.getId()))
              .andExpect(status().isOk());

      mockMvc.perform(get("/api/v1/items/{id}", testItem2.getId()))
              .andExpect(status().isOk());

      mockMvc.perform(get("/api/v1/items/{id}", 999L))
              .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("Authorization Tests")
  class AuthorizationTests {

    @Test
    @DisplayName("Should return 403 for all endpoints when user has no role")
    void shouldReturn403ForAllEndpointsWhenNoRole() throws Exception {
      when(authorisationService.hasAdminRole(any())).thenReturn(false);

      mockMvc.perform(post("/api/v1/items")
                      .with(user("user").authorities())
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(itemRequestDto)))
              .andExpect(status().isForbidden());

      mockMvc.perform(get("/api/v1/items/{id}", testItem.getId())
                      .with(user("user").authorities()))
              .andExpect(status().isForbidden());

      mockMvc.perform(patch("/api/v1/items/{id}", testItem.getId())
                      .with(user("user").authorities())
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(updateRequestDto)))
              .andExpect(status().isForbidden());

      mockMvc.perform(delete("/api/v1/items/{id}", testItem.getId())
                      .with(user("user").authorities()))
              .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 200 for all endpoints when user has ADMIN role")
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAccessForAdminRole() throws Exception {
      when(authorisationService.hasAdminRole(any())).thenReturn(true);

      mockMvc.perform(post("/api/v1/items")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(itemRequestDto)))
              .andExpect(status().isCreated());

      mockMvc.perform(get("/api/v1/items/{id}", testItem2.getId()))
              .andExpect(status().isOk());

      mockMvc.perform(patch("/api/v1/items/{id}", testItem2.getId())
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(updateRequestDto)))
              .andExpect(status().isOk());

      mockMvc.perform(delete("/api/v1/items/{id}", testItem2.getId()))
              .andExpect(status().isNoContent());
    }
  }
}