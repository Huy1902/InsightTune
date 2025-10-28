package com.pm.recommendservice.controller;

import com.pm.recommendservice.dto.RecTrackRequestDto;
import com.pm.recommendservice.dto.RecTrackResponseDto;
import com.pm.recommendservice.service.RecService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RecController {
  private final RecService recService;
  @GetMapping("/recommend")
  public RecTrackResponseDto getRec(@RequestParam Integer num_item, Authentication authentication) throws Exception{
    RecTrackRequestDto recTrackRequestDto = new RecTrackRequestDto();
    recTrackRequestDto.setK(num_item);
    return recService.recommend(recTrackRequestDto);
  }
}
