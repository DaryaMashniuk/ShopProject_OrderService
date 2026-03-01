package com.innowise.orderservice.specifications;

import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

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

  public static <T> Specification<T> in(String field, List<?> values) {
    return (root, query, cb) ->
            (values == null || values.isEmpty())
                    ? null
                    : root.get(field).in(values);
  }
}
