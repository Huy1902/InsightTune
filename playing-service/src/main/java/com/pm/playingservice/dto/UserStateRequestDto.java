package com.pm.playingservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserStateRequestDto (@NotBlank String trackId, @NotNull Integer positionMs){
}
