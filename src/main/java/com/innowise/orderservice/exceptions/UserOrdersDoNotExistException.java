package com.innowise.orderservice.exceptions;

public class UserOrdersDoNotExistException extends RuntimeException{

  public UserOrdersDoNotExistException(){
    super();
  }
  public UserOrdersDoNotExistException(String message){
    super(message);
  }
  public UserOrdersDoNotExistException(String message, Throwable cause){
    super(message, cause);
  }
}
