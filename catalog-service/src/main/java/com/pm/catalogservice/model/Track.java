
// Track.java  (updated)
package com.pm.catalogservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.*;

@Entity @Table(name = "tracks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Track {
  @Id @UuidGenerator
  private UUID id;

  @NotNull(message = "Title is required")
  private String title;

  // Track → Album (nullable for singles)
  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "album_id")           // FK to albums.id
  private Album album;

  // Track ↔ Artist via join table
  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
          name = "track_artists",
          joinColumns = @JoinColumn(name = "track_id"),
          inverseJoinColumns = @JoinColumn(name = "artist_id")
  )
  @Builder.Default
  private Set<Artist> artists = new LinkedHashSet<>();

  @NotNull(message = "Storage key is required")
  @Column(unique = true)
  private String storageKey;

  @Positive
  private int durationMs;

  private String coverImageKey;
}
