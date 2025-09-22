package com.pm.uploadservice.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter @Setter
@AllArgsConstructor
public class TrackUploadRequestDto {
  private MultipartFile file;
}
