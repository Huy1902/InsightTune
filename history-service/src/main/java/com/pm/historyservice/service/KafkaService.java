package com.pm.historyservice.service;

import com.google.protobuf.InvalidProtocolBufferException;
import com.pm.historyservice.repository.HistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import track.events.PlayTrackEvent;
import track.events.SearchSongEvent;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaService {
  private final HistoryService historyService;

  @KafkaListener(topics = "play_track", groupId = "history-service")
  public void kafkaConsumer(byte[] event) {
    try {
      PlayTrackEvent playTrackEvent = PlayTrackEvent.parseFrom(event);
      log.info("Received play track event: storage key {}, email {}",
              playTrackEvent.getStorageKey(), playTrackEvent.getEmail());

      historyService.ingestHistory(playTrackEvent);
    } catch (InvalidProtocolBufferException e) {
      log.error("Error deserializing event {}", e.getMessage());
    }

  }

  @KafkaListener(topics = "searched_song", groupId = "history-service")
  public void kafkaConsumerSearch(byte[] event) {
    try {
      SearchSongEvent searchSongEvent = SearchSongEvent.parseFrom(event);
      log.info("Received searched song event: email {}, search {}",
              searchSongEvent.getEmail(), searchSongEvent.getSearch());

      historyService.ingestSearched(searchSongEvent);
    } catch (InvalidProtocolBufferException e) {
      log.error("Error deserializing event {}", e.getMessage());
    }

  }
}
