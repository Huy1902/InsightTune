package com.pm.recommendservice.feign;

import com.pm.recommendservice.model.History;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "HISTORY-SERVICE", url="http://history-service.spotube.local:4003")
public interface HistoryServiceClient {
  @GetMapping("/history/internal")
  List<History> getHistory(@RequestParam String email);
}
