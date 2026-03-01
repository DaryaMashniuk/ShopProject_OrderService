package com.innowise.orderservice.mapper;

import com.innowise.orderservice.model.dto.response.PageResponseDto;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.function.Function;

@AllArgsConstructor
@Component
public class PageResponseMapper {

  public <T, R> PageResponseDto<R> mapToDto(Page<T> page, Function<T, R> elementMapper) {
    List<R> content = page.getContent()
            .stream()
            .map(elementMapper)
            .toList();
    return PageResponseDto.<R>builder()
            .content(content)
            .currentPage(page.getNumber())
            .totalPages(page.getTotalPages())
            .totalElements(page.getTotalElements())
            .pageSize(page.getSize())
            .first(page.isFirst())
            .last(page.isLast())
            .build();
  }

  public <T> PageResponseDto<T> mapToDto(Page<T> page) {
    return PageResponseDto.<T>builder()
            .content(page.getContent())
            .currentPage(page.getNumber())
            .totalPages(page.getTotalPages())
            .totalElements(page.getTotalElements())
            .pageSize(page.getSize())
            .first(page.isFirst())
            .last(page.isLast())
            .build();
  }
}