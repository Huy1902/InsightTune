package com.pm.recommendservice.feign;

import com.pm.recommendservice.config.FeignLogConfig;
import com.pm.recommendservice.dto.RecTrackRequestDto;
import com.pm.recommendservice.dto.RecTrackResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name="rec-sys", url="http://rec-sys.insighttune.local:4007")
public interface RecServiceClient {
  @PostMapping(value = "/recommend", consumes = "application/json")
  RecTrackResponseDto recommend(@RequestBody RecTrackRequestDto recTrackRequestDto);
}
