package com.pm.catalogservice.dto;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class AlbumResponseDto {
  private String name;
  private String description;
  private String coverImageKey;
}
