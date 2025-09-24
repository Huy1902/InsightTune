package com.pm.playingservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity @Table(name = "user_state")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserState {
  @Id @UuidGenerator
  private UUID id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(name = "track_id", nullable = false)
  private String trackId;

  @Column(name = "position_ms",nullable = false)
  private Integer positionMs;
}
