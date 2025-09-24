package com.pm.catalogservice.service;

import com.pm.catalogservice.model.Album;
import com.pm.catalogservice.model.Artist;
import com.pm.catalogservice.model.Track;
import com.pm.catalogservice.repository.AlbumRepository;
import com.pm.catalogservice.repository.ArtistRepository;
import com.pm.catalogservice.repository.TrackRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import track.events.CreatedTrackEvent;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service responsible for managing catalog entities such as {@link Track},
 * {@link Album}, and {@link Artist}.
 * <p>
 * The {@code CatalogService} provides logic when new tracks are
 * ingested from kafka consumer {@link KafkaService}
 * It ensures that:
 * <ul>
 *   <li>Tracks are created if they do not already exist in the catalog.</li>
 *   <li>Existing tracks are updated when a duplicate storage key is detected.</li>
 *   <li>Albums and artists referenced by the event are looked up in a
 *       case-insensitive manner and created if not found.</li>
 *   <li>Entity relationships (Track ↔ Album, Track ↔ Artist) are properly set.</li>
 * </ul>
 * <p>
 * This service is transactional: all modifications within an ingestion are
 * committed atomically, ensuring consistency between tracks, albums, and artists.
 *
 * @author Huy1902
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogService {
  private final ArtistRepository artistRepository;
  private final AlbumRepository albumRepository;
  private final TrackRepository trackRepository;

  /**
   * Ingests a {@link CreatedTrackEvent} into the catalog. Specifically,
   * <ul>
   *   <li>If a track already exists with the same {@code storageKey}, it is updated
   *       with new title, duration, cover image, album, and artists.</li>
   *   <li>If no track exists for the {@code storageKey}, a new {@link Track} is created.</li>
   *   <li>Album and artist references are resolved case-insensitively; new entities
   *       are persisted if no match is found.</li>
   *   <li>When the album field is blank or {@code null}, the track is treated as a
   *       single (no album association).</li>
   * </ul>
   *
   * @param createdTrackEvent the event containing metadata about the newly
   *                          created or updated track, including storage key,
   *                          title, album, artists, duration, and cover image.
   * @throws IllegalArgumentException if {@code createdTrackEvent} is {@code null}
   *                                  or does not contain a valid storage key.
   */
  @Transactional
  public void ingestTrack(CreatedTrackEvent createdTrackEvent) {
    final String storageKey = createdTrackEvent.getStorageKey();
    Optional<Track> trackOpt = trackRepository.findTrackByStorageKey(storageKey);

    if (trackOpt.isPresent()) {
      Track existingTrack = trackOpt.get();
      existingTrack.setTitle(createdTrackEvent.getTitle());
      existingTrack.setDurationMs(createdTrackEvent.getDurationMs());
      existingTrack.setCoverImageKey(createdTrackEvent.getCoverImageKey());

      if (!createdTrackEvent.getAlbum().isBlank()) {
        Album album = albumRepository.findByTitleIgnoreCase(createdTrackEvent.getAlbum())
                .orElseGet(() -> albumRepository.save(
                        Album.builder().title(createdTrackEvent.getAlbum()).build()
                ));
        existingTrack.setAlbum(album);
      } else {
        existingTrack.setAlbum(null);
      }

      Set<Artist> artists = createdTrackEvent.getArtistsList().stream()
              .map(name -> artistRepository.findByNameIgnoreCase(name)
                      .orElseGet(() -> artistRepository.save(
                              Artist.builder().name(name).build()
                      )))
              .collect(Collectors.toSet());
      existingTrack.setArtists(artists);

      log.info("Updated track {} with storageKey {}", existingTrack.getTitle(), storageKey);
    } else {
      Track.TrackBuilder trackBuilder = Track.builder()
              .title(createdTrackEvent.getTitle())
              .storageKey(storageKey)
              .durationMs(createdTrackEvent.getDurationMs())
              .coverImageKey(createdTrackEvent.getCoverImageKey());

      if (!createdTrackEvent.getAlbum().isBlank()) {
        Album album = albumRepository.findByTitleIgnoreCase(createdTrackEvent.getAlbum())
                .orElseGet(() -> albumRepository.save(
                        Album.builder().title(createdTrackEvent.getAlbum()).build()
                ));
        trackBuilder.album(album);
      }

      Set<Artist> artists = createdTrackEvent.getArtistsList().stream()
              .map(name -> artistRepository.findByNameIgnoreCase(name)
                      .orElseGet(() -> artistRepository.save(
                              Artist.builder().name(name).build()
                      )))
              .collect(Collectors.toSet());
      trackBuilder.artists(artists);

      Track newTrack = trackBuilder.build();
      trackRepository.save(newTrack);

      log.info("Created new track {} with storageKey {}", newTrack.getTitle(), storageKey);
    }
  }
}
