package com.pm.playingservice.controller;

import com.pm.playingservice.dto.*;
import com.pm.playingservice.service.AwsUrlService;
import com.pm.playingservice.service.KafkaService;
import com.pm.playingservice.service.UserStateService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@Slf4j
public class PlayingController {
  private final AwsUrlService awsUrlService;
  private final UserStateService userStateService;
  private final KafkaService kafkaService;

  @PostMapping("/play")
  @Operation(summary = "Receive a play request then send back a play response contain a mp3 link and image link")
  public ResponseEntity<PlayResponseDto> play(@RequestBody @Valid PlayRequestDto req,
                                              Authentication auth) throws Exception {
    String trackUrl = awsUrlService.getUrl(req.storageKey());
    String imageUrl = "";
    if (!req.coverImageKey().isBlank()) {
      imageUrl = awsUrlService.getUrl(req.coverImageKey());
    }

    kafkaService.sendCreatedTrack(new PlayTrackDto(auth.getName(), req.storageKey(), LocalDateTime.now()));

    return ResponseEntity.ok().body(new PlayResponseDto(trackUrl, imageUrl));
  }

  @GetMapping("/url")
  @Operation(summary = "Get an image link of a song")
  public ResponseEntity<LinkRespondDto> getLink(@RequestParam String key) throws Exception {
    String url = awsUrlService.getUrl(key);
    return ResponseEntity.ok().body(new LinkRespondDto(url));
  }

  @PostMapping("/user_state")
  @Operation(summary = "Update user state of an user")
  public ResponseEntity<UserStateRespondDto> updateUserState(@RequestBody UserStateRequestDto userStateRequestDto,
                                                             Authentication auth) throws Exception {
    String email = auth.getName();
    UserStateUpdateRespondDto userStateUpdateRespondDto = userStateService.upsert(new UserStateUpdateRequestDto(email,
            userStateRequestDto.trackId(), userStateRequestDto.positionMs()));
    log.info("Update user state for email: {} with status {}", email, userStateUpdateRespondDto.status());
    return ResponseEntity.ok().body(new UserStateRespondDto(userStateRequestDto.trackId(), userStateRequestDto.positionMs()));

  }

  @GetMapping("/user_state")
  @Operation(summary = "Find user state by email")
  public ResponseEntity<UserStateRespondDto> findUserState(Authentication auth) throws Exception {
    String email = auth.getName();
    UserStateRespondDto userStateRespondDto = userStateService.getUserState(email);
    return ResponseEntity.ok().body(userStateRespondDto);
  }

}
