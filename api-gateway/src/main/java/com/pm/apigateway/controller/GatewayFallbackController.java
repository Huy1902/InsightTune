package com.pm.apigateway.controller;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
class GatewayFallbackController {

  @GetMapping(value = "/__fallback/tracks", produces = MediaType.APPLICATION_JSON_VALUE)
  Mono<String> tracksFallback() {
    return Mono.just("{\"message\":\"Tracks service is temporarily unavailable\"}");
  }

  @GetMapping(value = "/__fallback/play", produces = MediaType.APPLICATION_JSON_VALUE)
  Mono<String> playFallback() {
    return Mono.just("{\"message\":\"Playing service is temporarily unavailable\"}");
  }

  @GetMapping(value = "/__fallback/upload", produces = MediaType.APPLICATION_JSON_VALUE)
  Mono<String> uploadFallback() {
    return Mono.just("{\"message\":\"Upload service is temporarily unavailable\"}");
  }
}
