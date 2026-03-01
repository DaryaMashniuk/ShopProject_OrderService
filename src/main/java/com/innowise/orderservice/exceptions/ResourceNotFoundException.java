package com.innowise.orderservice.exceptions;

import java.util.Collection;
import java.util.stream.Collectors;

public class ResourceNotFoundException extends RuntimeException {
  public ResourceNotFoundException(String message) {
    super(message);
  }
  public ResourceNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public ResourceNotFoundException(Throwable cause) {
    super(cause);
  }

  public ResourceNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
    super(String.format("%s not found with %s: '%s'",
            resourceName, fieldName, fieldValue));
  }

  public ResourceNotFoundException(String resourceName, String fieldName, Collection<?> fieldValues) {
    super(String.format("%s not found with %s: [%s]",
            resourceName, fieldName,
            fieldValues.stream().map(Object::toString).collect(Collectors.joining(", "))));
  }
}
