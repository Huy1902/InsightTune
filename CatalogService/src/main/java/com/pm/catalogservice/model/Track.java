package com.pm.catalogservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Track {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @NotNull(message = "Title is required")
  private String title;

  @NotNull(message = "Artist is required")
  private String artist;

  private String album;

  @NotNull(message = "Storage key is required")
  private String storageKey;

  @NotNull(message = "Duration is required")
  private int durationMs;

  private String coverImageKey;
}
