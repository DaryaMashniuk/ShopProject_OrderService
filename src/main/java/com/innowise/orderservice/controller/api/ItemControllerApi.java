package com.innowise.orderservice.controller.api;

import com.innowise.orderservice.model.dto.request.ItemsRequestDto;
import com.innowise.orderservice.model.dto.response.ErrorResponse;
import com.innowise.orderservice.model.dto.response.ItemsResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;


@Tag(name = "Item Management", description = "API for managing products/items in the catalog")
@RequestMapping("/api/v1/items")
public interface ItemControllerApi {

  @Operation(
          summary = "Create a new item",
          description = "Creates a new product/item in the catalog"
  )
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "201",
                  description = "Item successfully created",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = ItemsResponseDto.class)
                  )
          ),
          @ApiResponse(
                  responseCode = "400",
                  description = "Invalid item data provided",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = ErrorResponse.class),
                          examples = @ExampleObject(
                                  value = "{\"timestamp\":\"2024-01-18T10:30:00Z\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"Validation failed for 2 field(s)\",\"path\":\"/api/v1/items\",\"details\":{\"name\":\"Name is required\",\"price\":\"Price must be positive\"}}"
                          )
                  )
          ),
          @ApiResponse(
                  responseCode = "409",
                  description = "Item with this name already exists",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = ErrorResponse.class),
                          examples = @ExampleObject(
                                  value = "{\"timestamp\":\"2024-01-18T10:30:00Z\",\"status\":409,\"error\":\"Conflict\",\"message\":\"Item already exists with name: Laptop\",\"path\":\"/api/v1/items\"}"
                          )
                  )
          )
  })
  @PostMapping
  ResponseEntity<ItemsResponseDto> createItem(@RequestBody @Valid ItemsRequestDto requestDto);

  @Operation(
          summary = "Get item by ID",
          description = "Retrieves detailed information about a specific item"
  )
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "200",
                  description = "Item found and returned",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = ItemsResponseDto.class)
                  )
          ),
          @ApiResponse(
                  responseCode = "404",
                  description = "Item not found",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = ErrorResponse.class),
                          examples = @ExampleObject(
                                  value = "{\"timestamp\":\"2024-01-18T10:30:00Z\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Item not found with id: 999\",\"path\":\"/api/v1/items/999\"}"
                          )
                  )
          )
  })
  @GetMapping("/{id}")
  ResponseEntity<ItemsResponseDto> getItemById(@PathVariable("id") Long id);

  @Operation(
          summary = "Update item by ID",
          description = "Updates an existing item's information"
  )
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "200",
                  description = "Item updated successfully",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = ItemsResponseDto.class)
                  )
          ),
          @ApiResponse(
                  responseCode = "400",
                  description = "Invalid update data provided",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = ErrorResponse.class)
                  )
          ),
          @ApiResponse(
                  responseCode = "404",
                  description = "Item not found",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = ErrorResponse.class)
                  )
          ),
          @ApiResponse(
                  responseCode = "409",
                  description = "Item with new name already exists",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = ErrorResponse.class)
                  )
          )
  })
  @PatchMapping("/{id}")
  ResponseEntity<ItemsResponseDto> updateItemById(
          @PathVariable("id") Long id,
          @RequestBody @Valid ItemsRequestDto requestDto);

  @Operation(
          summary = "Delete item by ID",
          description = "Permanently deletes an item from the catalog"
  )
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "204",
                  description = "Item deleted successfully"
          ),
          @ApiResponse(
                  responseCode = "404",
                  description = "Item not found",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = ErrorResponse.class)
                  )
          ),
          @ApiResponse(
                  responseCode = "409",
                  description = "Item cannot be deleted because it's referenced in orders",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = ErrorResponse.class)
                  )
          )
  })
  @DeleteMapping("/{id}")
  ResponseEntity<Void> deleteItemById(@PathVariable("id") Long id);
}
