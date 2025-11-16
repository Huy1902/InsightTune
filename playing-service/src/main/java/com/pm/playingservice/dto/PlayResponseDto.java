package com.pm.playingservice.dto;

import jakarta.validation.constraints.NotBlank;

public record PlayResponseDto (@NotBlank String trackUrl, String coverImageUrl) {
}
