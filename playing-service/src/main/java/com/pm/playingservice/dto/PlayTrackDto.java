package com.pm.playingservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record PlayTrackDto (@Email String email, @NotBlank String storageKey, @NotNull LocalDateTime playedAt) {
}
