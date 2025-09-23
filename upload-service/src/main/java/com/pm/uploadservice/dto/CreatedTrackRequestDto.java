package com.pm.uploadservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter @Getter @Builder
public class CreatedTrackRequestDto {
  private String storageKey;
  private String title;
  private String album;
  private List<String> artists;
  private Integer durationMs;
  private String coverImageKey;
}
