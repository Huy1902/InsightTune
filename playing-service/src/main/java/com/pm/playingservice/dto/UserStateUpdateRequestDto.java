package com.pm.playingservice.dto;

import jakarta.validation.constraints.NotNull;

public record UserStateUpdateRequestDto (@NotNull String email, @NotNull String trackId, @NotNull Integer positionMs){
}
