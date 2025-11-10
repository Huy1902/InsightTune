package com.pm.recommendservice.service;

import com.pm.recommendservice.dto.TrackResponseDto;
import com.pm.recommendservice.feign.CatalogServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrackService {
  private final CatalogServiceClient catalogServiceClient;

  public List<String> getTrackIds() {
    return catalogServiceClient.getTrackIds().getSpotifyIds();
  }

  public List<TrackResponseDto> getAllTracks() {
    return catalogServiceClient.getAllTracks();
  }
}
