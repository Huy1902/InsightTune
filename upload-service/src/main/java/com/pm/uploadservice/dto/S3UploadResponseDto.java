package com.pm.uploadservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Builder
public class S3UploadResponseDto {
  private String title;
  private String storageKey;
  private String coverImageKey;
  private String status;
}
