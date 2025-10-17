package com.pm.historyservice.service;

import com.google.protobuf.Timestamp;
import com.pm.historyservice.models.History;
import com.pm.historyservice.models.SearchHistory;
import com.pm.historyservice.repository.HistoryRepository;
import com.pm.historyservice.repository.SearchedHistoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import track.events.PlayTrackEvent;
import track.events.SearchSongEvent;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HistoryService {
  private final HistoryRepository historyRepository;
  private final SearchedHistoryRepository searchedHistoryRepository;

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

  public void ingestSearched(SearchSongEvent searchSongEvent) {
    final Timestamp ts = searchSongEvent.getSearchedAt();
    LocalDateTime ldt = Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos())
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime();

    String search = searchSongEvent.getSearch();
      searchedHistoryRepository.findBySearch(search).ifPresent(searchedHistoryRepository::delete);

      SearchHistory history = SearchHistory.builder()
            .email(searchSongEvent.getEmail())
            .search(searchSongEvent.getSearch())
            .searchedAt(ldt).build();

    searchedHistoryRepository.save(history);
  }

  public List<SearchHistory> findAllSearchByEmail(String email) {
    List<SearchHistory> list = searchedHistoryRepository.findAllByEmail(email);
    Collections.reverse(list);
    return list;  }
}
