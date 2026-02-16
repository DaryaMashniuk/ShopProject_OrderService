package com.innowise.orderservice.specifications;

import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

@UtilityClass
public class SpecificationUtils {

  public static <T> Specification<T> getBefore(String field, LocalDateTime end) {
    return (root, query, cb) ->
            end == null
                    ? null
                    : cb.lessThanOrEqualTo(root.get(field), end);
  }

  public static <T> Specification<T> getAfter(String field, LocalDateTime start) {
    return (root, query, cb) ->
            start == null
                    ? null
                    : cb.greaterThanOrEqualTo(root.get(field), start);
  }

  public static <T> Specification<T> equalsString(String field, Object value) {
    return (root, query, cb) ->
            value == null
            ? null : cb.equal(root.get(field), value);
  }
}
