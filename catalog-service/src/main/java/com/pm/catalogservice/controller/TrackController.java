package com.pm.catalogservice.controller;

import com.pm.catalogservice.dto.request.NextSongRequestDto;
import com.pm.catalogservice.dto.request.SearchSongRequestDto;
import com.pm.catalogservice.dto.response.TrackResponseDto;
import com.pm.catalogservice.service.KafkaService;
import com.pm.catalogservice.service.TrackService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/tracks")
@RequiredArgsConstructor
public class TrackController {
  private final TrackService trackService;
  private final KafkaService kafkaService;

  @GetMapping
  @Operation(summary = "Get all tracks")
  public ResponseEntity<List<TrackResponseDto>> getAllTracks() {
    List<TrackResponseDto> tracks = trackService.getTracks();
    return ResponseEntity.ok().body(tracks);
  }
  @GetMapping("/search")
  @Operation(summary = "Query based on title or artist")
  public ResponseEntity<List<TrackResponseDto>> getAllTracksByTitle(@RequestParam String keyword, Principal principal) {
    String email = principal.getName();

    List<TrackResponseDto> tracks = trackService.getTracksByKeyword(keyword);
    SearchSongRequestDto searchSongRequest = SearchSongRequestDto.builder()
            .email(email)
            .search(keyword)
            .searchedAt(LocalDateTime.now())
            .build();

    kafkaService.sendSearchSongHistory(searchSongRequest);
    return ResponseEntity.ok().body(tracks);
  }

  @GetMapping("/next")
  @Operation(summary = "Get 5 next songs", description = "This current playing track's information in request")
  public ResponseEntity<List<TrackResponseDto>> getNextSong(@RequestBody NextSongRequestDto nextSongRequestDto) {
    List<TrackResponseDto> tracks = trackService.getNextSong(nextSongRequestDto);
    return ResponseEntity.ok().body(tracks);
  }
}
