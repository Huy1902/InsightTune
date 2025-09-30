package com.pm.uploadservice.dto;

import jakarta.validation.constraints.NotBlank;
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

  @NotBlank
  private List<String> artists;

  @NotNull
  private Integer durationMs;


  byte[] image;
  private  String imageType;
}
