package com.innowise.orderservice.facade;

import com.innowise.orderservice.feignclient.UserServiceCircuitBreaker;
import com.innowise.orderservice.mapper.OrderMapper;
import com.innowise.orderservice.model.Orders;
import com.innowise.orderservice.model.dto.request.OrderRequestDto;
import com.innowise.orderservice.model.dto.request.OrderSearchCriteriaDto;
import com.innowise.orderservice.model.dto.request.OrderUpdateDto;
import com.innowise.orderservice.model.dto.response.OrderResponseDto;
import com.innowise.orderservice.model.dto.response.PageResponseDto;
import com.innowise.orderservice.model.dto.response.UserOrdersListResponseDto;
import com.innowise.orderservice.model.dto.response.UserResponseDto;
import com.innowise.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderFacade {

  private final OrderService orderService;
  private final OrderMapper orderMapper;
  private final UserServiceCircuitBreaker userServiceCircuitBreaker;


  public OrderResponseDto createOrder(OrderRequestDto orderRequestDto) {
    UserResponseDto userInfo = userServiceCircuitBreaker.getUserInfoByEmail(orderRequestDto.getEmail());

    Orders order = orderService.createOrder(orderRequestDto.getItems(),userInfo.getId());

    return orderMapper.toOrderResponseDto(order,userInfo);
  }

  public OrderResponseDto getOrderById(Long id) {
    Orders order = orderService.getOrderById(id);
    UserResponseDto userInfo = userServiceCircuitBreaker.getUserInfoByUserId(order.getUserId());
    return orderMapper.toOrderResponseDto(order,userInfo);
  }


  public UserOrdersListResponseDto getOrdersByUserId(Long userId) {
    UserResponseDto userInfo = userServiceCircuitBreaker.getUserInfoByUserId(userId);

    List<Orders> orders = orderService.getOrdersByUserId(userId);

    return orderMapper.toUserOrdersListResponseDto(orders,userInfo);
  }

  public OrderResponseDto updateOrderById(Long id, OrderUpdateDto orderUpdateDto) {
    UserResponseDto userInfo = userServiceCircuitBreaker.getUserInfoByEmail(orderUpdateDto.getEmail());

    Orders order = orderService.updateOrderById(id,orderUpdateDto);
    return orderMapper.toOrderResponseDto(order,userInfo);
  }


  public void deleteOrderById(Long id) {
    orderService.deleteOrderById(id);
  }


  public PageResponseDto<OrderResponseDto> findAllOrders(OrderSearchCriteriaDto orderSearchCriteriaDto, Pageable pageable) {

    Page<Orders> orders = orderService.findAllOrders(orderSearchCriteriaDto,pageable);

    List<Long> userIds = orders
            .stream()
            .map(Orders::getUserId)
            .distinct()
            .toList();

    List<UserResponseDto> usersInfo = userServiceCircuitBreaker.getUsersByIds(userIds);
    Map<Long, UserResponseDto> userMap = usersInfo.stream()
            .collect(Collectors.toMap(UserResponseDto::getId, u -> u));

    return orderMapper.toPageResponseDto(orders,userMap);
  }
}
