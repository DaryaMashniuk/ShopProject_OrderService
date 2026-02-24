package com.innowise.orderservice.model.dto.request;

import com.innowise.orderservice.model.OrderStatus;
import com.innowise.orderservice.validator.ValidEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Search criteria for filtering orders")
public class OrderSearchCriteriaDto {

  @Schema(
          description = "Filter orders created after this date",
          example = "2024-01-01T00:00:00",
          type = "string",
          format = "date-time"
  )
  @Past(message = "From date must be in the past")
  private LocalDateTime fromDate;

  @Schema(
          description = "Filter orders created before this date",
          example = "2024-12-31T23:59:59",
          type = "string",
          format = "date-time"
  )
  @PastOrPresent(message = "To date must be in the past or present")
  private LocalDateTime toDate;

  @Schema(
          description = "Filter by multiple order statuses",
          example = "[\"PENDING\", \"APPROVED\"]",
          allowableValues = {"PENDING", "APPROVED", "DELIVERED"}
  )
  @ValidEnum(enumClass = OrderStatus.class, message = "Invalid order status")
  private List<String> status;
}