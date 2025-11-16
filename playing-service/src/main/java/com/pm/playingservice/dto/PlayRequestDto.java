package com.pm.playingservice.dto;


import jakarta.validation.constraints.NotBlank;

public record PlayRequestDto(@NotBlank String storageKey, String coverImageKey) {
}
