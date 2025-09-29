package com.pm.playingservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserStateUpdateRequestDto (@Email String email, @NotBlank String trackId, @NotNull Integer positionMs){
}
