package com.innowise.orderservice.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO for displaying user information")
public class UserResponseDto implements Serializable {
  @Schema(description = "Unique user identifier", example = "1")
  private Long id;
  @Schema(description = "User's first name", example = "John")
  private String name;
  @Schema(description = "User's last name", example = "Doe")
  private String surname;
  @Schema(description = "Email address", example = "john.doe@example.com")
  private String email;
  @Schema(description = "Date of birth", example = "1990-01-15")
  private LocalDate birthDate;
  @Schema(description = "Active status", example = "true")
  private boolean active;
  @Schema(description = "Record creation timestamp", example = "2024-01-15T10:30:00")
  private LocalDateTime createdAt;
  @Schema(description = "Record last update timestamp", example = "2024-01-15T10:30:00")
  private LocalDateTime updatedAt;
}
