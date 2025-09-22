package com.pm.catalogservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.*;

@Entity @Table(name = "albums")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = "tracks")
public class Album {
  @Id @UuidGenerator
  @Column(columnDefinition = "uuid")
  private UUID id;

  @NotNull
  private String title;

  @OneToMany(mappedBy = "album", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  @Builder.Default
  private List<Track> tracks = new ArrayList<>();
}
