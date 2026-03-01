package com.innowise.orderservice.specifications;

import com.innowise.orderservice.model.OrderStatus;
import com.innowise.orderservice.model.Orders;
import com.innowise.orderservice.model.dto.request.OrderSearchCriteriaDto;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

@UtilityClass
public class OrderSpecification {

  public static Specification<Orders> build(OrderSearchCriteriaDto criteria) {
    return Specification.where(hasStatus(criteria.getStatus()))
            .and(createdAfter(criteria.getFromDate()))
            .and(createdBefore(criteria.getToDate()));
  }

  public static Specification<Orders> hasStatus(List<String> status){
    if (status == null || status.isEmpty()){
      return null;
    }
    try {
      List<OrderStatus> orderStatuses = status
              .stream()
              .map(st -> OrderStatus.valueOf(st.toUpperCase().trim()))
              .toList();
      return SpecificationUtils.in("status", orderStatuses);
    } catch (IllegalArgumentException e) {
      return (root, query, cb) -> cb.disjunction();
    }
  }

  public static Specification<Orders> createdAfter(LocalDateTime from){
    return SpecificationUtils.getAfter("createdAt", from);
  }

  public static Specification<Orders> createdBefore(LocalDateTime to){
    return SpecificationUtils.getBefore("createdAt", to);
  }
}
