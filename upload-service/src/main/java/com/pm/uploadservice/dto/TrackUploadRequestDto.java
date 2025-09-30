package com.pm.uploadservice.dto;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter @Setter
@AllArgsConstructor
public class TrackUploadRequestDto {
  @NotNull
  private MultipartFile file;
}
