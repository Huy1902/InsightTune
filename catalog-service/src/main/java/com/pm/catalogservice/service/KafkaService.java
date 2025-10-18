package com.pm.catalogservice.service;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import com.pm.catalogservice.dto.request.SearchSongRequestDto;
import com.pm.catalogservice.exception.KafkaServiceException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import track.events.CreatedTrackEvent;
import track.events.SearchSongEvent;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaService {
  private final CatalogService catalogService;
  private final KafkaTemplate<String, byte[]> kafkaTemplate;
  private final Validator validator;

  @KafkaListener(topics = "created_track", groupId = "catalog-service")
  public void kafkaConsumer(byte[] event) {
    try {
      CreatedTrackEvent createdTrackEvent = CreatedTrackEvent.parseFrom(event);
      log.info("Received created track event: title {}, artist {}, album {}, duration {}, storage Key {}, cover Key {}",
              createdTrackEvent.getTitle(),
              createdTrackEvent.getArtists(0),
              createdTrackEvent.getAlbum(),
              createdTrackEvent.getDurationMs(),
              createdTrackEvent.getStorageKey(),
              createdTrackEvent.getCoverImageKey());

      catalogService.ingestTrack(createdTrackEvent);
    } catch (InvalidProtocolBufferException e) {
      log.error("Error deserializing event {}", e.getMessage());
    }

  }

  @Transactional
  public void sendSearchSongHistory(SearchSongRequestDto searchSongRequestDto) {
    LocalDateTime ldt = searchSongRequestDto.getSearchedAt();
    Instant instant = ldt.atZone(ZoneId.systemDefault()).toInstant();

    Timestamp ts = Timestamps.fromMillis(instant.toEpochMilli());

    SearchSongEvent searchSongEvent = SearchSongEvent.newBuilder()
            .setEmail(searchSongRequestDto.getEmail())
            .setSearch(searchSongRequestDto.getSearch())
            .setSearchedAt(ts)
            .build();

    Set<ConstraintViolation<SearchSongEvent>> violations = validator.validate(searchSongEvent);
    if (!violations.isEmpty()) {
      log.error(violations.iterator().next().getMessage());
      throw new KafkaServiceException(violations.iterator().next().getMessage());
    }

    try {
      log.info("Send Search Song Event: {}", searchSongEvent);
      kafkaTemplate.send("searched_song", searchSongEvent.toByteArray());
    } catch (Exception e) {
      log.error("Error Searched Song created event: {}", e.getMessage());
      throw new KafkaServiceException("Error Searched Song created event: " + e.getMessage());
    }
  }
}
