package com.innowise.orderservice.exceptions;

public class QuantityIsNotRegisteredException extends RuntimeException{
  public QuantityIsNotRegisteredException() {
    super();
  }

  public QuantityIsNotRegisteredException(String message) {
    super(message);
  }

  public QuantityIsNotRegisteredException(String message, Throwable cause) {
    super(message, cause);
  }
}
