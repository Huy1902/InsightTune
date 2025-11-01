package com.pm.recommendservice.feign;

import com.pm.recommendservice.config.FeignLogConfig;
import com.pm.recommendservice.dto.RecTrackRequestDto;
import com.pm.recommendservice.dto.RecTrackResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name="rec-sys", url="${rec.url:http://localhost:4007}", configuration = FeignLogConfig.class)
public interface RecServiceClient {
  @PostMapping(value = "/recommend", consumes = "application/json")
  RecTrackResponseDto recommend(@RequestBody RecTrackRequestDto recTrackRequestDto);
}
