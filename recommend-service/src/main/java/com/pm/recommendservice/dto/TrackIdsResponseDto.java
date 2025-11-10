package com.pm.recommendservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pm.recommendservice.model.History;
import lombok.Getter;

import java.util.List;

@Getter
public class TrackIdsResponseDto {
  @JsonProperty("spotifyId")
  List<String> spotifyIds;
}
