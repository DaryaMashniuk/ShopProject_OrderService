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

  @CircuitBreaker(name = "userservice", fallbackMethod = "fallback")
  public UserResponseDto getUserInfoByEmail(String email) {
    return userServiceClient
            .getUserInfoByEmail(email)
            .getContent()
            .getFirst();
  }

  @CircuitBreaker(name = "userservice", fallbackMethod = "fallback")
  public UserResponseDto getUserInfoByUserId(Long userId) {
    return userServiceClient.getUserInfoById(userId);
  }

  @CircuitBreaker(name = "userservice", fallbackMethod = "fallback")
  public List<UserResponseDto> getUsersByIds(List<Long> userIds) {
    return userServiceClient.getUsersByIds(userIds);
  }


  private void fallback(Throwable e){
    if (e instanceof feign.RetryableException) {
      logger.error("User Service is physically unreachable: {}", e.getMessage());
      throw new UserServiceIsDownException("User Service is unreachable (Connection Refused)");
    }
    if (e instanceof CallNotPermittedException) {
      logger.error("Circuit Breaker is OPEN! Stopping requests to UserService.");
    }
    if (e instanceof ResourceNotFoundException ex) {
      throw ex;
    }

    if (e instanceof AuthorizationDeniedException ex) {
      throw ex;
    }

    throw new UserServiceIsDownException("User service is down",e);
  }
}
