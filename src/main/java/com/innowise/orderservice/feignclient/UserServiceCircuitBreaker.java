package com.innowise.orderservice.feignclient;

import com.innowise.orderservice.exceptions.ResourceNotFoundException;
import com.innowise.orderservice.exceptions.UserServiceIsDownException;
import com.innowise.orderservice.model.dto.response.UserResponseDto;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class UserServiceCircuitBreaker {

  private final UserServiceClient userServiceClient;
  private static final Logger logger = LogManager.getLogger(UserServiceCircuitBreaker.class);

  @CircuitBreaker(name = "userservice", fallbackMethod = "fallbackResponseByEmail")
  public UserResponseDto getUserInfoByEmail(String email) {
    return userServiceClient
            .getUserInfoByEmail(email)
            .getContent()
            .getFirst();
  }

  @CircuitBreaker(name = "userservice", fallbackMethod = "fallbackResponseById")
  public UserResponseDto getUserInfoByUserId(Long userId) {
    return userServiceClient.getUserInfoById(userId);
  }

  @CircuitBreaker(name = "userservice", fallbackMethod = "fallbackResponseByIds")
  public List<UserResponseDto> getUsersByIds(List<Long> userIds) {
    return userServiceClient.getUsersByIds(userIds);
  }

  public UserResponseDto fallbackResponseByEmail(String email,Throwable e) {
    fallback(e);
    throw new UserServiceIsDownException("User service is down",e);
  }
  public UserResponseDto fallbackResponseById(Long userId,Throwable e) {
    fallback(e);
    throw new UserServiceIsDownException("User service is down",e);
  }
  public List<UserResponseDto> fallbackResponseByIds(List<Long> userIds,Throwable e) {
    fallback(e);
    throw new UserServiceIsDownException("User service is down",e);
  }

  private void fallback(Throwable e){
    if (e instanceof feign.RetryableException) {
      logger.error("User Service is physically unreachable: {}", e.getMessage());
      throw new UserServiceIsDownException("User Service is unreachable (Connection Refused)");
    }
    if (e instanceof CallNotPermittedException) {
      logger.error("Circuit Breaker is OPEN! Stopping requests to UserService.");
    }
    if (e instanceof ResourceNotFoundException) {
      throw (ResourceNotFoundException) e;
    }
    if (e instanceof AuthorizationDeniedException) {
      throw (AuthorizationDeniedException) e;
    }
  }
}
