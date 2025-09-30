package com.pm.uploadservice.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class CreatedTrackResponseDto {
  @NotEmpty
  private String kafkaStatus;
}
