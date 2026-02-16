package com.innowise.orderservice.specifications;

import com.innowise.orderservice.model.OrderStatus;
import com.innowise.orderservice.model.Orders;
import com.innowise.orderservice.model.dto.request.OrderSearchCriteriaDto;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

@UtilityClass
public class OrderSpecification {

  public static Specification<Orders> build(OrderSearchCriteriaDto criteria) {
    return Specification.allOf(hasStatus(criteria.getStatus()))
            .and(createdAfter(criteria.getFromDate()))
            .and(createdBefore(criteria.getToDate()));
  }

  public static Specification<Orders> hasStatus(String status){
    return SpecificationUtils.equalsString("status", status);
  }

  public static Specification<Orders> createdAfter(LocalDateTime from){
    return SpecificationUtils.getAfter("createdAt", from);
  }

  public static Specification<Orders> createdBefore(LocalDateTime to){
    return SpecificationUtils.getBefore("createdAt", to);
  }
}
