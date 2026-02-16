package com.innowise.orderservice.feignclient;

import com.innowise.orderservice.model.dto.response.PageResponseDto;
import com.innowise.orderservice.model.dto.response.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "userService",
        url = "http://localhost:8081",
        path = "/userservice"
)
public interface UserServiceClient {

  @GetMapping("/api/v1/users")
  PageResponseDto<UserResponseDto> getUserInfoByEmail(@RequestParam("email") String email);

  @GetMapping("/api/v1/users/{id}")
  UserResponseDto getUserInfoById(@PathVariable("id") Long id);

  @GetMapping("/api/v1/users/batch")
  List<UserResponseDto> getUsersByIds(@RequestParam("ids") List<Long> ids);
}
