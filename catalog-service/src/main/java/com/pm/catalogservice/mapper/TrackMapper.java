package com.pm.catalogservice.mapper;

import com.pm.catalogservice.dto.response.TrackIdsResponseDto;
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


  public static String extractIdFromStorageKey(String storageKey) {
    if (storageKey == null || storageKey.isBlank()) return null;
    String key = storageKey.trim();

    // take last path segment
    int slash = key.lastIndexOf('/');
    String file = (slash >= 0) ? key.substring(slash + 1) : key;

    // drop query/fragment if ever present
    int q = file.indexOf('?');
    if (q >= 0) file = file.substring(0, q);
    int h = file.indexOf('#');
    if (h >= 0) file = file.substring(0, h);

    // remove extension (".mp3", ".wav", etc.)
    int dot = file.lastIndexOf('.');
    if (dot > 0) file = file.substring(0, dot);

    return file.isEmpty() ? null : file;
  }
}
