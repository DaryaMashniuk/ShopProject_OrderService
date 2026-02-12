package com.innowise.orderservice.specifications;

import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Comparator;

@UtilityClass
public class SpecificationUtils {

  public static <T> Specification<T> getRange(String field, LocalDateTime start, LocalDateTime end) {
    return (root, query, cb) ->
            start == null || end == null
            ? null
            : cb.between(root.get(field), start, end);
  }

  public static <T> Specification<T> equalsString(String field, String value) {
    return (root, query, cb) ->
            value == null || value.isBlank()
            ? null : cb.equal(root.get(field), value);
  }
}
