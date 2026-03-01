package com.innowise.orderservice.exceptions;

public class UserServiceIsDownException extends RuntimeException{
  public UserServiceIsDownException(){
    super();
  }
  public UserServiceIsDownException(String message){
    super(message);
  }
  public UserServiceIsDownException(String message, Throwable cause){
    super(message, cause);
  }
}
