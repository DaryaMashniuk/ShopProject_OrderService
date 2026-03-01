package com.innowise.orderservice.service;

import com.innowise.orderservice.model.Items;
import com.innowise.orderservice.model.dto.request.ItemsRequestDto;
import com.innowise.orderservice.model.dto.request.OrderItemRequestDto;
import com.innowise.orderservice.model.dto.response.ItemsResponseDto;

import java.util.List;

/**
 * Service interface for managing product items in the catalog.
 * Provides comprehensive CRUD operations for items with business rules.
 *
 * @author Your Name
 * @version 1.0
 * @see Items
 * @see ItemsRequestDto
 * @see ItemsResponseDto
 */
public interface ItemsService {

  /**
   * Retrieves a list of items based on the requested item IDs.
   *
   * <p>This method is primarily used by the order creation process to
   * validate and fetch the actual item entities for requested item IDs.
   *
   * <p>The method validates that all requested items exist in the database.
   * If any item ID is invalid, the operation fails to ensure data integrity.
   *
   * @param orderItemRequestDtos list of item requests containing IDs
   * @return a list of corresponding item entities in the same order as requested
   * @throws NoSuchElementException if any requested item ID does not exist
   * @throws IllegalArgumentException if the input list is null
   */
  List<Items> getItems(List<OrderItemRequestDto> orderItemRequestDtos);

  /**
   * Creates a new item in the catalog.
   *
   * <p>Performs the following validations:
   * <ul>
   *   <li>Checks for duplicate item names (case-insensitive)</li>
   *   <li>Validates that price is positive</li>
   *   <li>Ensures name is not blank</li>
   * </ul>
   *
   * @param itemsRequestDto the DTO containing item details
   * @return the created item as a response DTO with generated ID
   * @throws ItemWithThatNameAlreadyExistsException if an item with the same name already exists
   * @throws jakarta.validation.ConstraintViolationException if validation fails
   */
  ItemsResponseDto createItem(ItemsRequestDto itemsRequestDto);

  /**
   * Updates an existing item with new information.
   *
   * <p>Supports partial updates and enforces the following rules:
   * <ul>
   *   <li>If name is being changed, checks for uniqueness of the new name</li>
   *   <li>If price is updated, ensures it remains positive</li>
   *   <li>Fields not provided in the DTO remain unchanged</li>
   * </ul>
   *
   * @param itemsRequestDto the DTO containing the update information
   * @param id              the ID of the item to update
   * @return the updated item as a response DTO
   * @throws ResourceNotFoundException if item with given ID does not exist
   * @throws ItemWithThatNameAlreadyExistsException if new name is already taken
   */
  ItemsResponseDto updateItemById(ItemsRequestDto itemsRequestDto, long id);

  /**
   * Permanently deletes an item from the catalog.
   *
   * <p>Note: This operation will fail if the item is referenced in any
   * existing orders due to referential integrity constraints.
   *
   * @param id the ID of the item to delete
   * @throws ResourceNotFoundException if item with given ID does not exist
   * @throws org.springframework.dao.DataIntegrityViolationException if item is referenced in orders
   */
  void deleteItemById(long id);

  /**
   * Retrieves an item by its unique identifier.
   *
   * <p>Returns the item details without any associated order information.
   * This is a read-only operation.
   *
   * @param id the ID of the item to retrieve
   * @return the item as a response DTO
   * @throws ResourceNotFoundException if item with given ID does not exist
   */
  ItemsResponseDto getItemById(long id);
}