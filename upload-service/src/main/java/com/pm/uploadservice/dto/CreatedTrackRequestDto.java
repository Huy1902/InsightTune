package com.pm.uploadservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter @Getter @Builder
public class CreatedTrackRequestDto {
  @NotBlank
  private String storageKey;

  @NotBlank
  private String title;

  @NotBlank
  private String album;

  private List<@NotBlank String> artists;

  @NotNull
  private Integer durationMs;

  @NotNull
  private String coverImageKey;
}
