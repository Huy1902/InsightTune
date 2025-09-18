package com.pm.catalogservice.service;

import com.pm.catalogservice.dto.TrackResponseDto;
import com.pm.catalogservice.mapper.TrackMapper;
import com.pm.catalogservice.model.Track;
import com.pm.catalogservice.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrackService {
  private final TrackRepository trackRepository;

  public List<TrackResponseDto> getTracks() {
    List<Track> tracks = trackRepository.findAll();

    return tracks.stream()
            .map(TrackMapper::toTrackResponseDto).toList();
  }
}
