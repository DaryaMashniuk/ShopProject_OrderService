package com.innowise.orderservice.mapper;

import com.innowise.orderservice.model.OrderItems;
import com.innowise.orderservice.model.Orders;
import com.innowise.orderservice.model.dto.request.OrderRequestDto;
import com.innowise.orderservice.model.dto.response.OrderItemResponseDto;
import com.innowise.orderservice.model.dto.response.OrderResponseDto;
import com.innowise.orderservice.model.dto.response.OrderResponseFromListDto;
import com.innowise.orderservice.model.dto.response.PageResponseDto;
import com.innowise.orderservice.model.dto.response.UserOrdersListResponseDto;
import com.innowise.orderservice.model.dto.response.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

@Mapper(
        componentModel = "spring",
        uses = {PageResponseMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public abstract class OrderMapper {

  @Autowired
  protected PageResponseMapper pageResponseMapper;

  @Mapping(target = "id", source = "order.id")
  @Mapping(target = "createdAt", source = "order.createdAt")
  @Mapping(target = "updatedAt", source = "order.updatedAt")
  @Mapping(target = "user", source = "userResponseDto")
  public abstract OrderResponseDto toOrderResponseDto(Orders order, UserResponseDto userResponseDto);

  @Mapping(target = "id", source = "order.id")
  @Mapping(target = "createdAt", source = "order.createdAt")
  @Mapping(target = "updatedAt", source = "order.updatedAt")
  public abstract OrderResponseFromListDto toOrderResponseFromListDto(Orders order);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "userId", ignore = true)
  @Mapping(target = "deleted", ignore = true)
  @Mapping(target = "totalPrice", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "orderItems", ignore = true)
  public abstract Orders toEntity(OrderRequestDto orderRequestDto);

  @Mapping(target = "itemId", source = "item.id")
  @Mapping(target = "itemName", source = "item.name")
  @Mapping(target = "itemPrice", source = "item.price")
  public abstract OrderItemResponseDto toOrderItemResponseDto(OrderItems orderItems);

  public abstract List<OrderItemResponseDto> toOrderItemResponseDtoList(List<OrderItems> items);

  public UserOrdersListResponseDto toUserOrdersListResponseDto(List<Orders> orders, UserResponseDto user) {
    UserOrdersListResponseDto dto = new UserOrdersListResponseDto();
    dto.setOrders(
            orders.stream()
                    .map(this::toOrderResponseFromListDto)
                    .toList()
    );
    dto.setUser(user);
    return dto;
  }


  public PageResponseDto<OrderResponseDto> toPageResponseDto(Page<Orders> ordersPage, Map<Long, UserResponseDto> userMap) {
    return pageResponseMapper.mapToDto(ordersPage, order ->
            toOrderResponseDto(order, userMap.getOrDefault(order.getUserId(),null))
    );
  }

}
