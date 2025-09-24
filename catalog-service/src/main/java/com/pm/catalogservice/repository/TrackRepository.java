package com.pm.catalogservice.repository;

import com.pm.catalogservice.model.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;
import java.util.UUID;

public interface TrackRepository extends JpaRepository<Track, UUID> {
  Optional<Track> findTracksByTitleIgnoreCase(@RequestParam String title);
  Optional<Track> findTrackByStorageKey(@RequestParam String storageKey);
}
