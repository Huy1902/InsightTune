package com.pm.catalogservice.dto;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class TrackResponseDto {
  private String id;
  private String title;
  private String storageKey;
  private String artist;
  private int durationMs;
  private String coverImageKey;
}
