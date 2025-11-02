package com.pm.recommendservice.controller;

import com.pm.recommendservice.dto.RecTrackRequestDto;
import com.pm.recommendservice.dto.RecTrackResponseDto;
import com.pm.recommendservice.dto.TrackResponseDto;
import com.pm.recommendservice.service.RecService;
import com.pm.recommendservice.service.TrackService;
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
  private final TrackService trackService;

  @GetMapping("/recommend")
  @Operation(summary = "Get recommend list")
  public ResponseEntity<List<TrackResponseDto>> getRec(@RequestParam Integer num_item, Authentication authentication) throws Exception{
    RecTrackRequestDto recTrackRequestDto = new RecTrackRequestDto();
    recTrackRequestDto.setK(num_item);
    return ResponseEntity.ok().body(recService.recommendTracks(recTrackRequestDto, authentication.getName()));
  }

  @GetMapping("/all_tracks")
  @Operation(summary = "Get all tracks")
  public ResponseEntity<List<TrackResponseDto>> getAllTracks(Authentication authentication) throws Exception{
    return ResponseEntity.ok().body(trackService.getAllTracks());
  }
}
