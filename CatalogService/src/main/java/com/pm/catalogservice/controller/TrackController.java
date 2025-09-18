package com.pm.catalogservice.controller;

import com.pm.catalogservice.dto.TrackResponseDto;
import com.pm.catalogservice.service.TrackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tracks")
@RequiredArgsConstructor
public class TrackController {
  private final TrackService trackService;

  @GetMapping
  public ResponseEntity<List<TrackResponseDto>> getAllTracks() {
    List<TrackResponseDto> tracks = trackService.getTracks();
    return ResponseEntity.ok().body(tracks);
  }
}
