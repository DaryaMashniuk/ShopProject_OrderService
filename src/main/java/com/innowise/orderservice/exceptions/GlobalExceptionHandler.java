package com.innowise.orderservice.exceptions;

import com.innowise.orderservice.model.dto.response.ErrorResponse;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

  private Logger logger = LogManager.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
          ResourceNotFoundException exception,
          WebRequest request) {
    logger.error("Resource not found: {}", exception.getMessage());
    ErrorResponse errorResponse = new ErrorResponse(
            Instant.now(),
            HttpStatus.NOT_FOUND.value(),
            HttpStatus.NOT_FOUND.getReasonPhrase(),
            exception.getMessage(),
            request.getDescription(false),
            null
    );

    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(CannotUpdateWithStatusException.class)
  public ResponseEntity<ErrorResponse> CannotUpdateWithStatusException(
          CannotUpdateWithStatusException exception,
          WebRequest request) {
    ErrorResponse errorResponse = new ErrorResponse(
            Instant.now(),
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            exception.getMessage(),
            request.getDescription(false),
            null
    );

    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(ItemWithThatNameAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleItemWithThatNameAlreadyExists(
          ItemWithThatNameAlreadyExistsException ex,
          WebRequest request) {
    logger.warn("Conflict detected: {}", ex.getMessage());
    ErrorResponse errorResponse = new ErrorResponse(
            Instant.now(),
            HttpStatus.CONFLICT.value(),
            HttpStatus.CONFLICT.getReasonPhrase(),
            ex.getMessage(),
            request.getDescription(false),
            null
    );

    return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
  }

  @ExceptionHandler(UserServiceIsDownException.class)
  public ResponseEntity<ErrorResponse> handleUserServiceIsDownException(
          UserServiceIsDownException ex,
          WebRequest request
  ) {
    ErrorResponse errorResponse = new ErrorResponse(
            Instant.now(),
            HttpStatus.SERVICE_UNAVAILABLE.value(),
            HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase(),
            "Something went wrong, the User service is not available",
            request.getDescription(false),
            null
    );
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
  }

  @ExceptionHandler(CallNotPermittedException.class)
  public ResponseEntity<ErrorResponse> handleCallNotPermittedException(
          CallNotPermittedException ex,
          WebRequest request) {
    logger.warn("Circuit Breaker is OPEN: {}", ex.getMessage());
    ErrorResponse errorResponse = new ErrorResponse(
            Instant.now(),
            HttpStatus.SERVICE_UNAVAILABLE.value(),
            "Circuit Breaker is OPEN",
            "Requests to User Service are temporarily blocked to prevent further failures",
            request.getDescription(false),
            null
    );
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
  }

  @ExceptionHandler(AuthorizationDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAccessDenied(
          AuthorizationDeniedException ex,
          HttpServletRequest request
  ) {
    ErrorResponse error = ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(403)
            .error("Forbidden")
            .message("Access denied")
            .path(request.getRequestURI())
            .build();

    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationExceptions(
          MethodArgumentNotValidException exception,
          WebRequest request
  ) {
    Map<String, Object> errors = new HashMap<>();

    exception.getBindingResult().getAllErrors().forEach(error -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });

    String errorMessage = "Validation failed for " + errors.size() + " field(s)";
    String requestDescription = request.getDescription(false);
    logger.warn("Validation failed for request {}: {}", requestDescription, errors);
    ErrorResponse errorResponse = new ErrorResponse(
            Instant.now(),
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            errorMessage,
            request.getDescription(false),
            errors
    );

    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolation(
          ConstraintViolationException ex,
          WebRequest request) {

    ErrorResponse errorResponse = new ErrorResponse(
            Instant.now(),
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            ex.getMessage(),
            request.getDescription(false),
            null
    );

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
          HttpMessageNotReadableException ex,
          WebRequest request) {

    ErrorResponse errorResponse = new ErrorResponse(
            Instant.now(),
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            ex.getMessage(),
            request.getDescription(false),
            null
    );

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneralException(
          Exception ex,
          WebRequest request) {

    logger.error("Unexpected error", ex);
    ErrorResponse errorResponse = new ErrorResponse(
            Instant.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
            "Internal server error",
            request.getDescription(false),
            null
    );

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<Object> handleNoResourceFoundException(NoResourceFoundException ex, WebRequest request) throws NoResourceFoundException {
    String path = ex.getResourcePath();
    if (path.contains("swagger-ui") || path.contains("v3/api-docs")) {
      throw ex;
    }

    ErrorResponse errorResponse = new ErrorResponse(
            Instant.now(),
            HttpStatus.NOT_FOUND.value(),
            HttpStatus.NOT_FOUND.getReasonPhrase(),
            "Resource not found: " + path,
            request.getDescription(false),
            null
    );
    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }
}
