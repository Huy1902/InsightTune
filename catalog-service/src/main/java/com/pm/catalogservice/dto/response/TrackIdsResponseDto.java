package com.pm.catalogservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
public class TrackIdsResponseDto {
  private List<String> spotifyId;
}
