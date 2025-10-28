package com.pm.recommendservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Setter
@Getter
@NoArgsConstructor
public class RecTrackRequestDto {
  @JsonProperty("user_history")
  private List<String> userHistory;
  @JsonProperty("k")
  private int k;
}
