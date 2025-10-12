package com.pm.catalogservice.service;

import com.pm.catalogservice.dto.TrackResponseDto;
import com.pm.catalogservice.mapper.TrackMapper;
import com.pm.catalogservice.model.Artist;
import com.pm.catalogservice.model.Track;
import com.pm.catalogservice.repository.ArtistRepository;
import com.pm.catalogservice.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class TrackService {
  private final TrackRepository trackRepository;
  private final ArtistRepository artistRepository;

  public List<TrackResponseDto> getTracks() {
    List<Track> tracks = trackRepository.findAll();

    return tracks.stream()
            .map(TrackMapper::toTrackResponseDto).toList();
  }

  public List<TrackResponseDto> getTracksByKeyword(String keyword) {
    List<Track> matchedTracks = trackRepository.findByTitleContainingIgnoreCase(keyword);
    List<Artist> matchedArtist = artistRepository.findByNameContainingIgnoreCase(keyword);

    if (matchedTracks.isEmpty() && matchedArtist.isEmpty()) {
      return Collections.emptyList();
    }

    List<Track> relatedTracks = trackRepository.findByArtistsIn(matchedArtist);
    // gộp và loại trùng
    return Stream.concat(matchedTracks.stream(), relatedTracks.stream())
            .distinct()
            .map(TrackMapper::toTrackResponseDto)
            .toList();
  }
}
