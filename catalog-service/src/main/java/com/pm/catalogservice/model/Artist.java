
// Artist.java
package com.pm.catalogservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.*;

@Entity @Table(name = "artists")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = "tracks")
public class Artist {
  @Id @UuidGenerator
  private UUID id;

  @NotNull
  @Column(unique = true)
  private String name;

  @ManyToMany(mappedBy = "artists", fetch = FetchType.LAZY)
  @Builder.Default
  private Set<Track> tracks = new LinkedHashSet<>();
}
