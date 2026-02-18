package com.innowise.orderservice.feignclient;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class FeignClientInterceptor implements RequestInterceptor {
  @Override
  public void apply(RequestTemplate requestTemplate) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    System.out.println("Intercepting request");
    if (auth != null && auth.isAuthenticated()) {
      requestTemplate
              .header("X-User-Id", auth.getName())
              .header("X-User-Role", auth.getAuthorities().stream()
                      .map(GrantedAuthority::getAuthority)
                      .map(a -> a.replace("ROLE_",""))
                      .findFirst().orElse("USER"));
    }
  }
}
