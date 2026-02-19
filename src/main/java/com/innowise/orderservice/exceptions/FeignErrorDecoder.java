package com.innowise.orderservice.exceptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.orderservice.model.dto.response.ErrorResponse;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class FeignErrorDecoder implements ErrorDecoder {
  private final ErrorDecoder decoder = new ErrorDecoder.Default();
  private final ObjectMapper objectMapper;

  public FeignErrorDecoder() {
    this.objectMapper = new ObjectMapper();

    this.objectMapper.findAndRegisterModules();
  }

  @Override
  public Exception decode(String methodKey, Response response) {
    String errorMessage = null;

    if (response.body() != null) {
      try (InputStream bodyIs = response.body().asInputStream()) {

        ErrorResponse errorResponse = objectMapper.readValue(bodyIs, ErrorResponse.class);
        errorMessage = errorResponse.getMessage();
      } catch (IOException e) {
        errorMessage = response.reason();
      }
    }

    if (response.status() == 404) {
      return new ResourceNotFoundException(errorMessage != null ? errorMessage : "User Service: Resource not found");
    }

    if (response.status() == 403) {
      return new AuthorizationDeniedException(errorMessage != null ? errorMessage : "User Service: Access denied");
    }

    return decoder.decode(methodKey, response);
  }
}
