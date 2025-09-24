package com.pm.catalogservice.service;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import track.events.CreatedTrackEvent;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaService {
  private final CatalogService catalogService;
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
}
