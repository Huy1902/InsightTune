package com.pm.playingservice.dto;

import jakarta.validation.constraints.NotBlank;

public record UserStateUpdateRespondDto(@NotBlank String status) {
}
