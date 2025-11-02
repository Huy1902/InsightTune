package com.pm.recommendservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class RecTrackResponseDto {
  @JsonProperty("topk")
  private List<String> topk;
}
