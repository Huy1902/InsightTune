package com.pm.favoriteservice.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TrackResponseDto {
    UUID id;
    String title;
    Set<String> artists;
    UUID albumId;
    String storageKey;
    int durationMs;
    String coverImageKey;
}
