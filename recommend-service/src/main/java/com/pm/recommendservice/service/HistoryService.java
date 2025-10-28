package com.pm.recommendservice.service;

import com.pm.recommendservice.feign.HistoryServiceClient;
import com.pm.recommendservice.model.History;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class HistoryService {
  private final HistoryServiceClient historyServiceClient;

  public List<String> getHistoryTrackKey() {
    return extractTrackIds(historyServiceClient.getHistory());
  }

  /** Extract IDs from a list of DTOs returned by your Feign client */
  private List<String> extractTrackIds(List<History> items) {
    if (items == null || items.isEmpty()) return List.of();
    return items.stream()
            .map(History::getStorageKey)
            .map(this::extractIdFromStorageKey)
            .filter(Objects::nonNull)
            .toList();
  }

  private String extractIdFromStorageKey(String storageKey) {
    if (storageKey == null || storageKey.isBlank()) return null;
    String key = storageKey.trim();

    // take last path segment
    int slash = key.lastIndexOf('/');
    String file = (slash >= 0) ? key.substring(slash + 1) : key;

    // drop query/fragment if ever present
    int q = file.indexOf('?'); if (q >= 0) file = file.substring(0, q);
    int h = file.indexOf('#'); if (h >= 0) file = file.substring(0, h);

    // remove extension (".mp3", ".wav", etc.)
    int dot = file.lastIndexOf('.');
    if (dot > 0) file = file.substring(0, dot);

    return file.isEmpty() ? null : file;
  }
}
