package com.pm.recommendservice.dto;

import com.pm.recommendservice.model.History;
import lombok.Getter;

import java.util.List;

@Getter
public class TrackIdsResponseDto {
  List<String> spotifyIds;
}
