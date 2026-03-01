package com.innowise.orderservice.exceptions;

public class CannotUpdateWithStatusException extends RuntimeException{
  public CannotUpdateWithStatusException(String message) {
    super(message);
  }
  public CannotUpdateWithStatusException(String message, Throwable cause) {
    super(message, cause);
  }
  public CannotUpdateWithStatusException(Throwable cause) {
    super(cause);
  }
}
