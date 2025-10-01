package com.pm.historyservice.service;

import com.google.protobuf.Timestamp;
import com.pm.historyservice.models.History;
import com.pm.historyservice.repository.HistoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import track.events.PlayTrackEvent;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HistoryService {
  private final HistoryRepository historyRepository;

  public History save(History history) {
    return historyRepository.save(history);
  }

  @Transactional
  public void ingestHistory(PlayTrackEvent playTrackEvent) {
    final Timestamp ts = playTrackEvent.getPlayedAt();
    LocalDateTime ldt = Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos())
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime();
    History history = History.builder()
            .storageKey(playTrackEvent.getStorageKey())
            .email(playTrackEvent.getEmail())
            .playedAt(ldt).build();
    historyRepository.save(history);
  }

  public List<History> findAllByEmail(String email) {
    return historyRepository.findAllByEmail(email);
  }
}
