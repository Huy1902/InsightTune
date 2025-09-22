package com.pm.playingservice.controller;

import com.pm.playingservice.dto.PlayRequestDto;
import com.pm.playingservice.dto.PlayResponseDto;
import com.pm.playingservice.service.AwsUrlService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PlayingController {
  private final AwsUrlService awsUrlService;

  public PlayingController(AwsUrlService awsUrlService) {
    this.awsUrlService = awsUrlService;
  }

  @PostMapping("/play")
  @Operation(summary = "Receive a play request then send back a play response contain a mp3 link")
  public ResponseEntity<PlayResponseDto> play(@RequestBody PlayRequestDto req, Authentication auth) throws Exception {
    String url = awsUrlService.getUrl(req.storageKey());
    return ResponseEntity.ok().body(new PlayResponseDto(url));
  }
}
