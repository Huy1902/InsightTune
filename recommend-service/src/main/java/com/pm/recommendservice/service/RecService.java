package com.pm.recommendservice.service;

import com.pm.recommendservice.dto.RecTrackRequestDto;
import com.pm.recommendservice.dto.RecTrackResponseDto;
import com.pm.recommendservice.feign.RecServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecService {
  private final HistoryService historyService;
  private final RecServiceClient recServiceClient;

  public RecTrackResponseDto recommend(RecTrackRequestDto recTrackRequestDto) {
    List<String> trackKeys = historyService.getHistoryTrackKey();
    recTrackRequestDto.setUserHistory(trackKeys);
    log.info("recTrackRequestDto: k = {}, userHistory = {}", recTrackRequestDto.getK(), recTrackRequestDto.getUserHistory());
    return recServiceClient.recommend(recTrackRequestDto);
  }
}
