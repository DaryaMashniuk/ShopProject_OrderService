package com.innowise.orderservice.exceptions;

public class UserDoesNotExistException extends RuntimeException{
  public UserDoesNotExistException(){
    super();
  }
  public UserDoesNotExistException(String message){
    super(message);
  }
  public UserDoesNotExistException(String message, Throwable cause){
    super(message, cause);
  }
}
