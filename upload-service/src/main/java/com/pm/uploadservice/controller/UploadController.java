package com.pm.uploadservice.controller;

import com.pm.uploadservice.dto.TrackUploadRequestDto;
import com.pm.uploadservice.dto.TrackUploadResponseDto;
import com.pm.uploadservice.service.UploadService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
public class UploadController {
  private final UploadService uploadService;

  @PostMapping("/upload")
  @Operation(summary = "Upload a mp3 file to Object Store Bucket")
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
