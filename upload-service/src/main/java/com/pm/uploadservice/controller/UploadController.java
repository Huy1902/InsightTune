package com.pm.uploadservice.controller;

import com.pm.uploadservice.dto.TrackUploadRequestDto;
import com.pm.uploadservice.dto.TrackUploadResponseDto;
import com.pm.uploadservice.exception.UploadServiceException;
import com.pm.uploadservice.service.UploadService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.security.NoSuchAlgorithmException;

@RequiredArgsConstructor
@RestController
@Slf4j
public class UploadController {
  private final UploadService uploadService;

  @PostMapping("/upload")
  @Operation(summary = "Upload a mp3 file to Object Store Bucket")
  public ResponseEntity<TrackUploadResponseDto> upload(@RequestParam MultipartFile file) {

    TrackUploadResponseDto trackUploadResponseDto = null;
    try {
      trackUploadResponseDto = uploadService.uploadTrack(new TrackUploadRequestDto(file,""));
    } catch (UploadServiceException e) {
      log.error(e.getMessage());
    }
    return ResponseEntity.ok().body(trackUploadResponseDto);
  }

  @PostMapping("/upload_with_key")
  @Operation(summary = "Upload a mp3 file to Object Store Bucket")
  public ResponseEntity<TrackUploadResponseDto> upload_with_key(@RequestParam MultipartFile file,
                                                                @RequestParam String key) {

    TrackUploadResponseDto trackUploadResponseDto = null;
    try {
      trackUploadResponseDto = uploadService.uploadTrack(new TrackUploadRequestDto(file, key));
    } catch (UploadServiceException e) {
      log.error(e.getMessage());
    }
    return ResponseEntity.ok().body(trackUploadResponseDto);
  }

  @RestControllerAdvice
  public static class GlobalExceptionHandler {
    @ExceptionHandler({MultipartException.class, MissingServletRequestPartException.class})
    public ResponseEntity<String> handleMultipartErrors(Exception e) {
      return ResponseEntity.badRequest().body("Invalid upload request: " + e.getMessage());
    }
  }
}
