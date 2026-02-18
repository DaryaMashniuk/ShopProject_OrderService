package com.innowise.orderservice.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for API error response")
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
  @Schema(description = "Error timestamp", example = "2024-01-18T22:11:17.239Z")
  private Instant timestamp;
  @Schema(description = "HTTP status code", example = "400")
  private int status;
  @Schema(description = "HTTP status reason phrase", example = "Bad Request")
  private String error;
  @Schema(description = "Error message", example = "Validation failed for 2 field(s)")
  private String message;
  @Schema(description = "Request path", example = "/api/v1/users")
  private String path;
  @Schema(description = "Additional error details (e.g., validation errors)")
  private Map<String, Object> details;

  public ErrorResponse(Instant timestamp, int status, String error,
                       String message, String path) {
    this(timestamp, status, error, message, path, null);
  }
}
