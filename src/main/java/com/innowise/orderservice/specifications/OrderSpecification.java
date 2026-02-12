package com.innowise.orderservice.specifications;

import com.innowise.orderservice.model.Orders;
import com.innowise.orderservice.model.dto.OrderSearchCriteriaDto;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

@UtilityClass
public class OrderSpecification {

  public static Specification<Orders> build(OrderSearchCriteriaDto criteria) {
    return Specification.allOf(hasStatus(String.valueOf(criteria.getStatus())))
            .and(createdInRange(criteria.getFromDate(),criteria.getToDate()));
  }

  public static Specification<Orders> hasStatus(String status){
    return SpecificationUtils.equalsString("status", status);
  }

  public static Specification<Orders> createdInRange(LocalDateTime from, LocalDateTime to){
    return SpecificationUtils.getRange("created_at", from, to);
  }
}
