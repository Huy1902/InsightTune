package com.pm.uploadserivce.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter @Setter
@AllArgsConstructor
public class TrackUploadRequestDto {
  private MultipartFile file;
}
