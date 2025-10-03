package com.pm.playingservice.service;

import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import com.pm.playingservice.dto.PlayTrackDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import track.events.PlayTrackEvent;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;


@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaService {
  private final KafkaTemplate<String, byte[]> kafkaTemplate;

  public void sendCreatedTrack(@Valid PlayTrackDto playTrackDto) {
    LocalDateTime ldt = playTrackDto.playedAt();
    Instant instant = ldt.atZone(ZoneId.systemDefault()).toInstant();

    Timestamp ts = Timestamps.fromMillis(instant.toEpochMilli());

    PlayTrackEvent playTrackEvent = PlayTrackEvent.newBuilder()
            .setEmail(playTrackDto.email())
            .setStorageKey(playTrackDto.storageKey())
            .setPlayedAt(ts)
            .build();

    try {
      log.info("Send Play Track Event: {}", playTrackEvent);
      kafkaTemplate.send("play_track", playTrackEvent.toByteArray());
    } catch (Exception e) {
      log.error("Error sending Track play event: {}", e.getMessage());
    }
  }
}
