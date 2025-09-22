package com.pm.catalogservice.service;

import com.pm.catalogservice.model.Track;
import com.pm.catalogservice.repository.AlbumRepository;
import com.pm.catalogservice.repository.ArtistRepository;
import com.pm.catalogservice.repository.TrackRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import track.events.CreatedTrackEvent;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogService {
  private final ArtistRepository artistRepository;
  private final AlbumRepository albumRepository;
  private final TrackRepository trackRepository;

  @Transactional
  public void ingestTrack(CreatedTrackEvent createdTrackEvent) {
    final String storageKey = createdTrackEvent.getStorageKey();
    Optional<Track> track = trackRepository.findTrackByStorageKey(storageKey);

    if(track.isPresent()) {
      log.info("Track already exists for storage key {}", storageKey);
      Track existingTrack = track.get();

    }
  }


}
