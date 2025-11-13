package com.pm.recommendservice.feign;

import com.pm.recommendservice.dto.TrackIdsResponseDto;
import com.pm.recommendservice.dto.TrackResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name="CATALOG-SERVICE", url="http://catalog-service.insighttune.local:4000")
public interface CatalogServiceClient {
  @GetMapping("/tracks/internal")
  TrackIdsResponseDto getTrackIds();

  @GetMapping("/tracks")
  List<TrackResponseDto> getAllTracks();
}
