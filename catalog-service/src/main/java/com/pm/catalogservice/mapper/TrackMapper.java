package com.pm.catalogservice.mapper;

import com.pm.catalogservice.dto.response.TrackResponseDto;
import com.pm.catalogservice.model.Artist;
import com.pm.catalogservice.model.Track;

import java.util.Set;
import java.util.stream.Collectors;

public class TrackMapper {
  public static TrackResponseDto toTrackResponseDto(Track track) {
    new TrackResponseDto();

    Set<String> artistNames = track.getArtists()
            .stream()
            .map(Artist::getName)
            .collect(Collectors.toSet());

    return TrackResponseDto.builder()
            .id(track.getId())
            .title(track.getTitle())
            .artists(artistNames)
            .albumId(track.getAlbum().getId())
            .storageKey(track.getStorageKey())
            .durationMs(track.getDurationMs())
            .coverImageKey(track.getCoverImageKey())
            .build();
  }
}
