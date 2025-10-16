package com.pm.catalogservice.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.Set;
import java.util.UUID;

public record NextSongRequestDto(@NotBlank Set<String> artists, @NotBlank UUID albumId,
                                 @NotBlank UUID currentTrackId) {
}
