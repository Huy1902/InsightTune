package com.pm.catalogservice.dto.response;

import lombok.*;

import java.util.Set;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class TrackResponseDto {
  private UUID id;
  private String title;
  private Set<String> artists;
  private UUID albumId;
  private String storageKey;
  private int durationMs;
  private String coverImageKey;
}
