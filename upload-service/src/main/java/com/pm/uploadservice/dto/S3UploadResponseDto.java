package com.pm.uploadservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Builder
public class S3UploadResponseDto {
  @NotBlank
  private String title;

  @NotBlank
  private String storageKey;

  private String coverImageKey;

  @NotBlank
  private String status;
}
