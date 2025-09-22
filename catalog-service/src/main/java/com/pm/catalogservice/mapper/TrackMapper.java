package com.pm.catalogservice.mapper;

import com.pm.catalogservice.dto.TrackResponseDto;
import com.pm.catalogservice.model.Track;

public class TrackMapper {
  public static TrackResponseDto toTrackResponseDto(Track track) {
    new TrackResponseDto();
    return TrackResponseDto.builder()
            .title(track.getTitle())
            .storageKey(track.getStorageKey())
            .durationMs(track.getDurationMs())
            .coverImageKey(track.getCoverImageKey())
            .build();
  }
}
