package com.pm.uploadserivce.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Setter @Getter
@AllArgsConstructor
public class MetaRequestDto {
  @NotNull(message = "File is required")
  private MultipartFile file;
}
