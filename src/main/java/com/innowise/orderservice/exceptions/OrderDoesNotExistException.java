package com.innowise.orderservice.exceptions;

public class OrderDoesNotExistException extends RuntimeException{
  public OrderDoesNotExistException(){
    super();
  }
  public OrderDoesNotExistException(String message){
    super(message);
  }
  public OrderDoesNotExistException(String message, Throwable cause){
    super(message, cause);
  }
}
