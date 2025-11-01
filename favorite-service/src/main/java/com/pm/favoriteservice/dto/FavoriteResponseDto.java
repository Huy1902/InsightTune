package com.pm.favoriteservice.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FavoriteResponseDto {
    Long id;
    String email;
    UUID songId;
    LocalDateTime created_at;
}
