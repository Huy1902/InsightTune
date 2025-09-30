package com.pm.historyservice.mapper;

import com.pm.historyservice.dto.HistoryRequest;
import com.pm.historyservice.models.History;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class HistoryMapper {

    public History convertHistoryRequestToHistory(HistoryRequest historyRequest){
      return History.builder()
              .storageKey(historyRequest.getTrackId())
              .playedAt(LocalDateTime.now()).build();
    }
}
