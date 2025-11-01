package com.pm.recommendservice.feign;

import com.pm.recommendservice.dto.TrackIdsResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name="catalog-service", url="localhost:4000")
public interface CatalogServiceClient {
  @GetMapping("/catalog/internal")
  TrackIdsResponseDto getTrackIds();
}
