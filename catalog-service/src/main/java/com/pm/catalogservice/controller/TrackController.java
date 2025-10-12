package com.pm.catalogservice.controller;

import com.pm.catalogservice.dto.TrackResponseDto;
import com.pm.catalogservice.service.TrackService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/tracks")
@RequiredArgsConstructor
public class TrackController {
  private final TrackService trackService;

  @GetMapping
  @Operation(summary = "Get all tracks")
  public ResponseEntity<List<TrackResponseDto>> getAllTracks() {
    List<TrackResponseDto> tracks = trackService.getTracks();
    return ResponseEntity.ok().body(tracks);
  }
  @GetMapping("/search")
  @Operation(summary = "Query based on title")
  public ResponseEntity<List<TrackResponseDto>> getAllTracksByTitle(@RequestParam String keyword) {
    List<TrackResponseDto> tracks = trackService.getTracksByKeyword(keyword);
    return ResponseEntity.ok().body(tracks);
  }
}
