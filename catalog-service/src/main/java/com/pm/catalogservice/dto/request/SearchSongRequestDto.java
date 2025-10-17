package com.pm.catalogservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
public class SearchSongRequestDto {
    @Email
    String email;

    @NotBlank
    String search;

    @NotBlank
    LocalDateTime searchedAt;
}
