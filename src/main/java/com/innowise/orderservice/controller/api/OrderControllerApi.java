package com.innowise.orderservice.controller.api;

import com.innowise.orderservice.model.dto.request.OrderRequestDto;
import com.innowise.orderservice.model.dto.request.OrderUpdateDto;
import com.innowise.orderservice.model.dto.response.ErrorResponse;
import com.innowise.orderservice.model.dto.response.OrderResponseDto;
import com.innowise.orderservice.model.dto.response.PageResponseDto;
import com.innowise.orderservice.model.dto.response.UserOrdersListResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;


@Tag(name = "Order Management", description = "API for managing orders in the system")
@RequestMapping("/api/v1/orders")
public interface OrderControllerApi {

  @Operation(
          summary = "Create a new order",
          description = "Creates a new order for a user. The user is identified by email, and order items must be valid."
  )
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "201",
                  description = "Order successfully created",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = OrderResponseDto.class)
                  )
          ),
          @ApiResponse(
                  responseCode = "400",
                  description = "Invalid order data or user service unavailable",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = ErrorResponse.class),
                          examples = {
                                  @ExampleObject(
                                          name = "Validation Error",
                                          value = "{\"timestamp\":\"2024-01-18T10:30:00Z\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"Validation failed for 2 field(s)\",\"path\":\"/api/v1/orders\",\"details\":{\"email\":\"Email should be valid\",\"items\":\"Order must have at least one item\"}}"
                                  )
                          }
                  )
          ),
          @ApiResponse(
                  responseCode = "404",
                  description = "User not found or items not found",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = ErrorResponse.class)
                  )
          ),
          @ApiResponse(
                  responseCode = "503",
                  description = "User Service is unavailable or Circuit Breaker is OPEN",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = ErrorResponse.class),
                          examples = {
                                  @ExampleObject(
                                          name = "Service Down",
                                          value = "{\"timestamp\":\"2026-02-18T19:00:00Z\",\"status\":503,\"error\":\"Service Unavailable\",\"message\":\"Something went wrong, the User service is not available\",\"path\":\"/api/v1/orders/user/2\"}"
                                  ),
                                  @ExampleObject(
                                          name = "Circuit Breaker Open",
                                          value = "{\"timestamp\":\"2026-02-18T19:05:00Z\",\"status\":503,\"error\":\"Service Unavailable\",\"message\":\"Circuit Breaker is OPEN\",\"path\":\"/api/v1/orders/user/2\"}"
                                  )
                          }
                  )
          )
  })
  @PostMapping
  ResponseEntity<OrderResponseDto> createOrder(@RequestBody @Valid OrderRequestDto orderRequestDto);

  @Operation(
          summary = "Get order by ID",
          description = "Retrieves detailed information about a specific order including user details and items"
  )
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "200",
                  description = "Order found and returned",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = OrderResponseDto.class)
                  )
          ),
          @ApiResponse(
                  responseCode = "404",
                  description = "Order not found",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = ErrorResponse.class),
                          examples = @ExampleObject(
                                  value = "{\"timestamp\":\"2024-01-18T10:30:00Z\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Order not found with id: 999\",\"path\":\"/api/v1/orders/999\"}"
                          )
                  )
          ),
          @ApiResponse(
                  responseCode = "503",
                  description = "User Service is unavailable or Circuit Breaker is OPEN",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = ErrorResponse.class),
                          examples = {
                                  @ExampleObject(
                                          name = "Service Down",
                                          value = "{\"timestamp\":\"2026-02-18T19:00:00Z\",\"status\":503,\"error\":\"Service Unavailable\",\"message\":\"Something went wrong, the User service is not available\",\"path\":\"/api/v1/orders/user/2\"}"
                                  ),
                                  @ExampleObject(
                                          name = "Circuit Breaker Open",
                                          value = "{\"timestamp\":\"2026-02-18T19:05:00Z\",\"status\":503,\"error\":\"Service Unavailable\",\"message\":\"Circuit Breaker is OPEN\",\"path\":\"/api/v1/orders/user/2\"}"
                                  )
                          }
                  )
          )
  })
  @GetMapping("/{id}")
  ResponseEntity<OrderResponseDto> getOrderById(@PathVariable("id") Long id);

  @Operation(
          summary = "Get all orders for a user",
          description = "Retrieves all orders placed by a specific user with aggregated information"
  )
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "200",
                  description = "Orders retrieved successfully",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = UserOrdersListResponseDto.class)
                  )
          ),
          @ApiResponse(
                  responseCode = "404",
                  description = "User not found or user has no orders",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = ErrorResponse.class)
                  )
          ),
          @ApiResponse(
                  responseCode = "503",
                  description = "User Service is unavailable or Circuit Breaker is OPEN",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = ErrorResponse.class),
                          examples = {
                                  @ExampleObject(
                                          name = "Service Down",
                                          value = "{\"timestamp\":\"2026-02-18T19:00:00Z\",\"status\":503,\"error\":\"Service Unavailable\",\"message\":\"Something went wrong, the User service is not available\",\"path\":\"/api/v1/orders/user/2\"}"
                                  ),
                                  @ExampleObject(
                                          name = "Circuit Breaker Open",
                                          value = "{\"timestamp\":\"2026-02-18T19:05:00Z\",\"status\":503,\"error\":\"Service Unavailable\",\"message\":\"Circuit Breaker is OPEN\",\"path\":\"/api/v1/orders/user/2\"}"
                                  )
                          }
                  )
          )
  })
  @GetMapping("/user/{userId}")
  ResponseEntity<UserOrdersListResponseDto> getOrdersByUserId(@PathVariable("userId") Long userId);

  @Operation(
          summary = "Update order by ID",
          description = "Updates an existing order. Only PENDING orders can be modified."
  )
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "200",
                  description = "Order updated successfully",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = OrderResponseDto.class)
                  )
          ),
          @ApiResponse(
                  responseCode = "400",
                  description = "Cannot modify non-PENDING order or validation error",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = ErrorResponse.class)
                  )
          ),
          @ApiResponse(
                  responseCode = "404",
                  description = "Order not found",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = ErrorResponse.class)
                  )
          ),
          @ApiResponse(
                  responseCode = "503",
                  description = "User Service is unavailable or Circuit Breaker is OPEN",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = ErrorResponse.class),
                          examples = {
                                  @ExampleObject(
                                          name = "Service Down",
                                          value = "{\"timestamp\":\"2026-02-18T19:00:00Z\",\"status\":503,\"error\":\"Service Unavailable\",\"message\":\"Something went wrong, the User service is not available\",\"path\":\"/api/v1/orders/user/2\"}"
                                  ),
                                  @ExampleObject(
                                          name = "Circuit Breaker Open",
                                          value = "{\"timestamp\":\"2026-02-18T19:05:00Z\",\"status\":503,\"error\":\"Service Unavailable\",\"message\":\"Circuit Breaker is OPEN\",\"path\":\"/api/v1/orders/user/2\"}"
                                  )
                          }
                  )
          )
  })
  @PatchMapping("/{id}")
  ResponseEntity<OrderResponseDto> updateOrderById(
          @PathVariable("id") Long id,
          @RequestBody @Valid OrderUpdateDto orderUpdateDto);

  @Operation(
          summary = "Soft delete order by ID",
          description = "Performs a soft delete on an order (marks as deleted without removing from database)"
  )
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "204",
                  description = "Order deleted successfully"
          ),
          @ApiResponse(
                  responseCode = "404",
                  description = "Order not found",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = ErrorResponse.class)
                  )
          )
//          ,
//          @ApiResponse(
//                  responseCode = "400",
//                  description = "Cannot delete PENDING order",
//                  content = @Content(
//                          mediaType = "application/json",
//                          schema = @Schema(implementation = ErrorResponse.class)
//                  )
//          )
  })
  @DeleteMapping("/{id}")
  ResponseEntity<Void> deleteOrderById(@PathVariable("id") Long id);

  @Operation(
          summary = "Find all orders with filters",
          description = "Retrieves paginated list of orders with optional filters by date range and status"
  )
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "200",
                  description = "Orders retrieved successfully",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = PageResponseDto.class)
                  )
          ),
          @ApiResponse(
                  responseCode = "400",
                  description = "Invalid filter parameters",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = ErrorResponse.class)
                  )
          ),
          @ApiResponse(
                  responseCode = "503",
                  description = "User Service is unavailable or Circuit Breaker is OPEN",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = ErrorResponse.class),
                          examples = {
                                  @ExampleObject(
                                          name = "Service Down",
                                          value = "{\"timestamp\":\"2026-02-18T19:00:00Z\",\"status\":503,\"error\":\"Service Unavailable\",\"message\":\"Something went wrong, the User service is not available\",\"path\":\"/api/v1/orders/user/2\"}"
                                  ),
                                  @ExampleObject(
                                          name = "Circuit Breaker Open",
                                          value = "{\"timestamp\":\"2026-02-18T19:05:00Z\",\"status\":503,\"error\":\"Service Unavailable\",\"message\":\"Circuit Breaker is OPEN\",\"path\":\"/api/v1/orders/user/2\"}"
                                  )
                          }
                  )
          )
  })
  @GetMapping
  ResponseEntity<PageResponseDto<OrderResponseDto>> findAllOrders(
          @Parameter(
                  description = "Filter orders created after this date (inclusive)",
                  example = "2024-01-01T00:00:00"
          )
          @RequestParam(required = false) LocalDateTime fromDate,

          @Parameter(
                  description = "Filter orders created before this date (inclusive)",
                  example = "2024-12-31T23:59:59"
          )
          @RequestParam(required = false) LocalDateTime toDate,

          @Parameter(
                  description = "Filter by order status",
                  example = "PENDING",
                  schema = @Schema(allowableValues = {"PENDING", "APPROVED", "DELIVERED"})
          )
          @RequestParam(required = false) String status,

          @ParameterObject Pageable pageable);
}
