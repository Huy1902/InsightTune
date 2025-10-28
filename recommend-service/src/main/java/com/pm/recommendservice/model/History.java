package com.pm.recommendservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class History {

  private Long id;

  UUID trackId;

  private String email;

  private String storageKey;

  private LocalDateTime playedAt;
}
