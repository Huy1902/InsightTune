package com.pm.uploadserivce.controller;

import com.pm.uploadserivce.dto.S3UploadRequestDto;
import com.pm.uploadserivce.dto.S3UploadResponseDto;
import com.pm.uploadserivce.dto.TrackUploadRequestDto;
import com.pm.uploadserivce.dto.TrackUploadResponseDto;
import com.pm.uploadserivce.service.S3Service;
import com.pm.uploadserivce.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
public class UploadController {
  private final UploadService uploadService;

  @PostMapping("/upload")
  public ResponseEntity<TrackUploadResponseDto> upload(@RequestParam MultipartFile file)  {

    TrackUploadResponseDto trackUploadResponseDto = uploadService.uploadTrack(new TrackUploadRequestDto(file));
    return ResponseEntity.ok().body(trackUploadResponseDto);
  }

//  @GetMapping("/download")
//  public ResponseEntity<byte[]> download(@RequestParam String filename) {
//    byte[] data = s3Service.downloadFile(filename);
//    return ResponseEntity.ok()
//            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
//            .body(data);
//  }
}
