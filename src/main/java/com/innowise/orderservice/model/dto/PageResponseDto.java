package com.innowise.orderservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "DTO for paginated response")
public class PageResponseDto<T>{
  @Schema(description = "List of items on the current page")
  private List<T> content;

  @Schema(description = "Current page number (0-based)", example = "0")
  private int currentPage;

  @Schema(description = "Total number of pages", example = "5")
  private int totalPages;

  @Schema(description = "Total number of elements", example = "50")
  private long totalElements;

  @Schema(description = "Number of elements per page", example = "10")
  private int pageSize;

  @Schema(description = "Whether current page is the first page", example = "true")
  private boolean first;

  @Schema(description = "Whether current page is the last page", example = "false")
  private boolean last;
}

