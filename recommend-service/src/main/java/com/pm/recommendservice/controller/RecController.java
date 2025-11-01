package com.pm.recommendservice.controller;

import com.pm.recommendservice.dto.RecTrackRequestDto;
import com.pm.recommendservice.dto.RecTrackResponseDto;
import com.pm.recommendservice.service.RecService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RecController {
  private final RecService recService;

  @GetMapping("/recommend")
  @Operation(summary = "Get recommend list")
  public ResponseEntity<RecTrackResponseDto> getRec(@RequestParam Integer num_item, Authentication authentication) throws Exception{
    RecTrackRequestDto recTrackRequestDto = new RecTrackRequestDto();
    recTrackRequestDto.setK(num_item);
    return ResponseEntity.ok().body(recService.recommend(recTrackRequestDto, authentication.getName()));
  }
}
