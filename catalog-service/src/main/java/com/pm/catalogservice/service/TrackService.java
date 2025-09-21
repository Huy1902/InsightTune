package com.pm.catalogservice.service;

import com.pm.catalogservice.dto.TrackResponseDto;
import com.pm.catalogservice.mapper.TrackMapper;
import com.pm.catalogservice.model.Track;
import com.pm.catalogservice.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TrackService {
  private final TrackRepository trackRepository;

  public List<TrackResponseDto> getTracks() {
    List<Track> tracks = trackRepository.findAll();

    return tracks.stream()
            .map(TrackMapper::toTrackResponseDto).toList();
  }

  public List<TrackResponseDto> getTracksByName(@RequestParam String title) {
    Optional<Track> tracks = trackRepository.findTracksByTitleIgnoreCase(title);

    return tracks.stream()
            .map(TrackMapper::toTrackResponseDto).toList();
  }
}
