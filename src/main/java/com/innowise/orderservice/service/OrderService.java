package com.innowise.orderservice.service;

import com.innowise.orderservice.model.Orders;
import com.innowise.orderservice.model.dto.request.OrderItemRequestDto;
import com.innowise.orderservice.model.dto.request.OrderSearchCriteriaDto;
import com.innowise.orderservice.model.dto.request.OrderUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for managing orders in the system.
 * Provides comprehensive CRUD operations with business logic for order processing.
 *
 * @see Orders
 * @see OrderUpdateDto
 * @see OrderSearchCriteriaDto
 */
public interface OrderService {

  /**
   * Creates a new order for a specific user.
   *
   * <p>This method performs the following operations:
   * <ul>
   *   <li>Validates the existence of all requested items</li>
   *   <li>Creates order items with the specified quantities</li>
   *   <li>Calculates the total price based on item prices and quantities</li>
   *   <li>Sets initial order status to {@link OrderStatus#PENDING}</li>
   *   <li>Persists the order with its items in the database</li>
   * </ul>
   *
   * @param items  the list of items to be included in the order, must not be null or empty
   * @param userId the ID of the user placing the order, must be a valid user ID
   * @return the newly created order entity with generated ID and calculated total price
   * @throws ResourceNotFoundException if any item ID is invalid or user not found
   * @throws IllegalArgumentException if items list is empty or null
   */
  Orders createOrder(List<OrderItemRequestDto> items, Long userId);

  /**
   * Retrieves an order by its unique identifier.
   *
   * <p>This method fetches a complete order entity including all associated
   * order items. The order is fetched in a read-only transaction for optimal
   * performance.
   *
   * @param id the unique identifier of the order to retrieve
   * @return the order entity if found
   * @throws ResourceNotFoundException if no order exists with the given ID
   */
  Orders getOrderById(Long id);

  /**
   * Retrieves all orders placed by a specific user.
   *
   * <p>Returns a list of all orders belonging to the specified user,
   * including both active and completed orders. Orders are returned
   * without any specific ordering by default.
   *
   * @param userId the ID of the user whose orders to retrieve
   * @return a list of orders belonging to the user
   * @throws ResourceNotFoundException if the user ID is invalid (optional behavior)
   */
  List<Orders> getOrdersByUserId(Long userId);

  /**
   * Updates an existing order with new information.
   *
   * <p>This method supports partial updates and enforces the following rules:
   * <ul>
   *   <li>Only orders with {@link OrderStatus#PENDING} status can be modified</li>
   *   <li>If items are updated, the total price is recalculated</li>
   *   <li>Status can be updated regardless of order items</li>
   * </ul>
   *
   * @param id              the ID of the order to update
   * @param orderUpdateDto  the DTO containing the update information
   * @return the updated order entity
   * @throws ResourceNotFoundException if order not found
   * @throws IllegalStateException if attempting to modify a non-PENDING order
   * @throws ResourceNotFoundException if user with new email not found
   */
  Orders updateOrderById(Long id, OrderUpdateDto orderUpdateDto);

  /**
   * Performs a soft delete of an order.
   *
   * <p>This method marks the order as deleted by setting the {@code deleted} flag to {@code true}
   * rather than physically removing it from the database. The soft delete is implemented
   * using Hibernate's {@link org.hibernate.annotations.SQLDelete} annotation.
   *
   * <p><strong>Behavior:</strong>
   * <ul>
   *   <li>The order remains in the database with {@code deleted = true}</li>
   *   <li>The order will not appear in any subsequent queries due to {@code @SQLRestriction}</li>
   *   <li>All associated order items remain in the database (cascade settings may affect this)</li>
   *   <li>Once soft-deleted, the order cannot be recovered through standard API operations</li>
   * </ul>
   *
   * @param id the ID of the order to soft delete
   * @throws ResourceNotFoundException if no order exists with the given ID
   * @throws IllegalStateException if attempting to delete an already deleted order
   */
  void deleteOrderById(Long id);

  /**
   * Retrieves a paginated list of orders with optional filtering.
   *
   * <p>Supports the following filters:
   * <ul>
   *   <li>Date range filtering (fromDate/toDate) - orders created within interval</li>
   *   <li>Status filtering - orders with specific status</li>
   *   <li>Combination of filters using AND logic</li>
   * </ul>
   *
   * <p>If no filters are provided, returns all orders with pagination.
   *
   * @param orderSearchCriteriaDto the DTO containing filter criteria
   * @param pageable               pagination information (page number, size, sort)
   * @return a page of orders matching the criteria
   */
  Page<Orders> findAllOrders(OrderSearchCriteriaDto orderSearchCriteriaDto, Pageable pageable);
}