package com.pm.uploadserivce.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Builder
public class MetaResponseDto {
  private String title;
  private String album;
  private List<String> artists;
  private Integer durationMs;
  byte[] image;
  private  String imageType;
}
