package com.pm.catalogservice.dto;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class TrackResponseDto {
  private String title;
  private String storageKey;
  private int durationMs;
  private String coverImageKey;
}
