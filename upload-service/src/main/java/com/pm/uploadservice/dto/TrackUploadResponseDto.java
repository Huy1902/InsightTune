package com.pm.uploadservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@Builder
public class TrackUploadResponseDto {
  private String title = "";
  private List<String> artists;
  private String storageKey;
  private String coverImageKey;
  private String kafkaStatus;
  private String s3Status;
}
