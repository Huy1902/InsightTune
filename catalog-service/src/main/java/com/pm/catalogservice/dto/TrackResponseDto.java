package com.pm.catalogservice.dto;

import com.pm.catalogservice.model.Artist;
import lombok.*;

import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class TrackResponseDto {
  private String title;
  private Set<String> artists;
  private String storageKey;
  private int durationMs;
  private String coverImageKey;
}
