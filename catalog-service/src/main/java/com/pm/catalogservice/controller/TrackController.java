package com.pm.catalogservice.controller;

import com.pm.catalogservice.dto.request.SearchSongRequestDto;
import com.pm.catalogservice.dto.response.TrackIdsResponseDto;
import com.pm.catalogservice.dto.response.TrackResponseDto;
import com.pm.catalogservice.service.KafkaService;
import com.pm.catalogservice.service.TrackService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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

  @GetMapping("/next/{currentTrackId}")
  @Operation(summary = "Get 5 next songs", description = "This current playing track's id in path")
  public ResponseEntity<List<TrackResponseDto>> getNextSong(@PathVariable UUID currentTrackId) {
    List<TrackResponseDto> tracks = trackService.getNextSong(currentTrackId);
    return ResponseEntity.ok().body(tracks);
  }

  @PostMapping("/by-ids")
  public ResponseEntity<List<TrackResponseDto>> getAllTracksById(@RequestBody Set<UUID> ids) {
      return ResponseEntity.ok().body(trackService.getAllTrackByIds(ids));
  }

  @GetMapping("/internal")
  @PermitAll
  public ResponseEntity<TrackIdsResponseDto> getAllTracksIds() {
    return ResponseEntity.ok().body(new TrackIdsResponseDto(trackService.getTracksIds()));
  }
}
