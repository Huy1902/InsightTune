package com.pm.uploadservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@Builder
public class TrackUploadResponseDto {
  @NotBlank
  private String title;

  private List<@NotBlank String> artists;

  @NotBlank
  private String storageKey;

  private String coverImageKey;

  @NotBlank
  private String kafkaStatus;

  @NotBlank
  private String s3Status;
}
