package com.innowise.orderservice.controller;

import com.innowise.orderservice.controller.api.OrderControllerApi;
import com.innowise.orderservice.facade.OrderFacade;
import com.innowise.orderservice.model.dto.request.OrderRequestDto;
import com.innowise.orderservice.model.dto.request.OrderSearchCriteriaDto;
import com.innowise.orderservice.model.dto.request.OrderUpdateDto;
import com.innowise.orderservice.model.dto.response.OrderResponseDto;
import com.innowise.orderservice.model.dto.response.PageResponseDto;
import com.innowise.orderservice.model.dto.response.UserOrdersListResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController  implements OrderControllerApi {

  private final OrderFacade orderFacade;

  @Override
  @PreAuthorize("@authorisationService.hasAdminRole(authentication)")
  @PostMapping
  public ResponseEntity<OrderResponseDto> createOrder(@RequestBody @Valid OrderRequestDto orderRequestDto) {
    OrderResponseDto createdOrder = orderFacade.createOrder(orderRequestDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
  }

  @Override
  @PreAuthorize("@authorisationService.hasAdminRole(authentication)")
  @GetMapping("/{id}")
  public ResponseEntity<OrderResponseDto> getOrderById(@PathVariable("id") Long id) {
    OrderResponseDto orderResponseDto = orderFacade.getOrderById(id);
    return ResponseEntity.ok().body(orderResponseDto);
  }

  @Override
  @PreAuthorize(
          "@authorisationService.hasAdminRole(authentication) or " +
                  "@authorisationService.isSelf(#userId, authentication)"
  )
  @GetMapping("/users/{userId}")
  public ResponseEntity<UserOrdersListResponseDto> getOrdersByUserId(@PathVariable("userId") Long userId) {
    UserOrdersListResponseDto userOrdersListResponseDto = orderFacade.getOrdersByUserId(userId);
    return ResponseEntity.ok().body(userOrdersListResponseDto);
  }

  @Override
  @PreAuthorize("@authorisationService.hasAdminRole(authentication)")
  @PatchMapping("/{id}")
  public ResponseEntity<OrderResponseDto> updateOrderById(@PathVariable("id") Long id,
                                                          @RequestBody @Valid OrderUpdateDto orderUpdateDto) {
    OrderResponseDto updatedOrder = orderFacade.updateOrderById(id, orderUpdateDto);
    return ResponseEntity.ok().body(updatedOrder);
  }

  @Override
  @PreAuthorize("@authorisationService.hasAdminRole(authentication)")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteOrderById(@PathVariable("id") Long id) {
    orderFacade.deleteOrderById(id);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @Override
  @PreAuthorize("@authorisationService.hasAdminRole(authentication)")
  @GetMapping
  public ResponseEntity<PageResponseDto<OrderResponseDto>> findAllOrders(
          @RequestParam(required = false) LocalDateTime fromDate,
          @RequestParam(required = false) LocalDateTime toDate,
          @RequestParam(required = false) List<String> status,
          @ParameterObject Pageable pageable) {
    OrderSearchCriteriaDto criteria = OrderSearchCriteriaDto.builder()
            .fromDate(fromDate)
            .toDate(toDate)
            .status(status)
            .build();
    PageResponseDto<OrderResponseDto> orders = orderFacade.findAllOrders(criteria, pageable);
    return ResponseEntity.ok().body(orders);
  }
}
