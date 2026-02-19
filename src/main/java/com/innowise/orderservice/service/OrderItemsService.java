package com.innowise.orderservice.service;

import com.innowise.orderservice.model.OrderItems;
import com.innowise.orderservice.model.Orders;
import com.innowise.orderservice.model.dto.request.OrderItemRequestDto;

import java.util.List;

/**
 * Service interface for managing order items within orders.
 * Handles the creation and management of individual line items in orders.
 *
 * @see OrderItems
 * @see Orders
 * @see OrderItemRequestDto
 */
public interface OrderItemsService {

  /**
   * Creates order items for a specific order based on the requested items.
   *
   * <p>This method performs the following operations:
   * <ul>
   *   <li>Validates the existence of all requested items through {@link ItemsService}</li>
   *   <li>Creates {@link OrderItems} entities for each requested item</li>
   *   <li>Associates each order item with the provided order</li>
   *   <li>Sets the quantity for each item as specified in the request</li>
   * </ul>
   *
   * <p>The method does NOT save the order items to the database directly.
   * It returns the entities to be saved by the calling service through
   * cascade operations.
   *
   * @param order         the order to which these items belong
   * @param requestItems  the list of items with their requested quantities
   * @return a list of created order items (not yet persisted)
   * @throws NoSuchElementException if any requested item ID does not exist
   * @throws IllegalArgumentException if requestItems is null or contains invalid data
   */
  List<OrderItems> createOrderItems(Orders order, List<OrderItemRequestDto> requestItems);
}