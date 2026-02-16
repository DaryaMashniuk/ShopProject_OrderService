package com.innowise.orderservice.feignclient;

import com.innowise.orderservice.exceptions.UserServiceIsDownException;
import com.innowise.orderservice.model.dto.response.UserResponseDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class UserServiceCircuitBreaker {

  private final UserServiceClient userServiceClient;

  @CircuitBreaker(name = "userservice", fallbackMethod = "fallbackResponse")
  public UserResponseDto getUserInfoByEmail(String email) {
    return userServiceClient
            .getUserInfoByEmail(email)
            .getContent()
            .getFirst();
  }

  @CircuitBreaker(name = "userservice", fallbackMethod = "fallbackResponse")
  public UserResponseDto getUserInfoByUserId(Long userId) {
    return userServiceClient.getUserInfoById(userId);
  }

  @CircuitBreaker(name = "userservice", fallbackMethod = "fallbackResponse")
  public List<UserResponseDto> getUsersByIds(List<Long> userIds) {
    return userServiceClient.getUsersByIds(userIds);
  }

  public UserResponseDto fallbackResponse(Throwable e) {
    throw new UserServiceIsDownException("User service is down",e);
  }
}
