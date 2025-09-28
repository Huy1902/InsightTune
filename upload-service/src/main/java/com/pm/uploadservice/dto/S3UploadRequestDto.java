package com.pm.uploadservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Builder
@Getter
@Setter
public class S3UploadRequestDto {
  @NotBlank(message = "Key is required")
  private String key;

  @NotNull(message = "File is required")
  private MultipartFile file;

  private byte[] image;

  private String imageType;

}
