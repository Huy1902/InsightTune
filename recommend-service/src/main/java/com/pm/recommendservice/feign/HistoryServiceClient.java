package com.pm.recommendservice.feign;

import com.pm.recommendservice.model.History;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "history-service", url = "http://localhost:4003")
public interface HistoryServiceClient {
  @GetMapping("/history")
  List<History> getHistory();
}
