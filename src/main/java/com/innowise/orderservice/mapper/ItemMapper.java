package com.innowise.orderservice.mapper;

import com.innowise.orderservice.model.Items;
import com.innowise.orderservice.model.dto.request.ItemsRequestDto;
import com.innowise.orderservice.model.dto.response.ItemsResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ItemMapper {
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "orderItems", ignore = true)
  Items toEntity(ItemsRequestDto dto);


  ItemsResponseDto toResponseDto(Items entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "orderItems", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void updateEntityFromDto(ItemsRequestDto dto, @MappingTarget Items entity);
}
