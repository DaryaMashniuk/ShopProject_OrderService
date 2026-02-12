package com.innowise.orderservice.model.dto;

import com.innowise.orderservice.model.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderSearchCriteriaDto {

  private LocalDateTime fromDate;
  private LocalDateTime toDate;
  private OrderStatus status;
}
