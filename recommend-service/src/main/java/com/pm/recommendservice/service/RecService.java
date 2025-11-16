
package com.pm.recommendservice.service;

import com.pm.recommendservice.dto.RecTrackRequestDto;
import com.pm.recommendservice.dto.RecTrackResponseDto;
import com.pm.recommendservice.dto.TrackResponseDto;
import com.pm.recommendservice.feign.RecServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecService {

  private final HistoryService historyService;
  private final RecServiceClient recServiceClient;
  private final TrackService trackService;

  public List<TrackResponseDto> recommendTracks(RecTrackRequestDto recTrackRequestDto, String email) {
    List<String> historyKeys = historyService.getHistoryTrackKey(email);
    List<String> historyIds = historyKeys.stream()
            .map(this::extractIdFromStorageKey)
            .toList();
    recTrackRequestDto.setUserHistory(historyIds);

    List<String> candidateIds = trackService.getTrackIds();
    log.info("candidateIds: {}", candidateIds);
    recTrackRequestDto.setCandidateIds(candidateIds);

    RecTrackResponseDto resp = recServiceClient.recommend(recTrackRequestDto);
    log.info("recTrackRequestDto: k={}, userHistory={}", recTrackRequestDto.getK(), recTrackRequestDto.getUserHistory());
    log.info("rec response topk: {}", resp.getTopk());

    Set<String> topkSet = new HashSet<>(resp.getTopk());

    List<TrackResponseDto> allTracks = trackService.getAllTracks();
    return allTracks.stream()
            .filter(t -> {
              String id = extractIdFromStorageKey(t.getStorageKey());
              return topkSet.contains(id);
            })
            .sorted(Comparator.comparingInt(t ->
                    indexOf(topkSet, resp.getTopk(), extractIdFromStorageKey(t.getStorageKey()))))
            .collect(Collectors.toList());
  }

  private String extractIdFromStorageKey(String storageKey) {
    if (storageKey == null) return null;
    int slash = storageKey.lastIndexOf('/');
    int dot = storageKey.lastIndexOf('.');
    if (slash == -1) slash = -1;
    if (dot == -1 || dot < slash) dot = storageKey.length();
    return storageKey.substring(slash + 1, dot);
  }

  private int indexOf(Set<String> topkSet, List<String> topkList, String id) {
    int idx = topkList.indexOf(id);
    return idx == -1 ? topkList.size() : idx;
  }
}
