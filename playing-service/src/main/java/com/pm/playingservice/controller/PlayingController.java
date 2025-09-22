package com.pm.playingservice.controller;

import com.pm.playingservice.model.TrackRequest;
import com.pm.playingservice.service.AwsUrlService;
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
  public ResponseEntity<?> play(@RequestBody TrackRequest req, Authentication auth) throws Exception {
    String url = awsUrlService.getUrl(req.track_key());
    return ResponseEntity.ok().body(new Object() {
      public final String signedUrl = url;
    });
  }
}
