package com.innowise.orderservice.exceptions;

public class ItemWithThatNameAlreadyExistsException extends RuntimeException {
  public ItemWithThatNameAlreadyExistsException(String message) {
    super(message);
  }
  public ItemWithThatNameAlreadyExistsException(String message, Throwable cause) {
    super(message, cause);
  }
  public ItemWithThatNameAlreadyExistsException(Throwable cause) {
    super(cause);
  }
}
