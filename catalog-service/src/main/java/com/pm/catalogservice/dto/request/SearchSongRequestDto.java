package com.pm.catalogservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchSongRequestDto {
    @Email
    String email;

    @NotBlank
    String search;

    @NotBlank
    LocalDateTime searchedAt;
}
