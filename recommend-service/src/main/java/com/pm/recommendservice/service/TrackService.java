package com.pm.recommendservice.service;

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
}
