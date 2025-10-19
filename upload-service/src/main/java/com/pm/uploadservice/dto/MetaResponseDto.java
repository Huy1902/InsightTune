package com.pm.uploadservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Builder
public class MetaResponseDto {
  @NotBlank
  private String title;

  @NotBlank
  private String album;

  @NotEmpty
  @Valid
  private List<@NotBlank String> artists;

  @NotNull
  private Integer durationMs;


  byte[] image;
  private  String imageType;
}
