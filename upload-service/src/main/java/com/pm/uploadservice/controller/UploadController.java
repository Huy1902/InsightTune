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

  /**
   * Upload an MP3 file to the object storage bucket.
   *
   * @param file MP3 file to upload
   * @return {@link TrackUploadResponseDto} containing metadata and storage keys
   */
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

  /**
   * Upload an MP3 file to the object storage bucket with a custom key.
   *
   * @param file MP3 file to upload
   * @param key custom key to store the file under
   * @return {@link TrackUploadResponseDto} containing metadata and storage keys
   */
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

  /**
   * Global exception handler for invalid upload requests.
   */
  @RestControllerAdvice
  public static class GlobalExceptionHandler {

    /**
     * Handles errors when the upload request is malformed or missing parts.
     *
     * @param e exception thrown during upload
     * @return HTTP 400 (Bad Request) with error message
     */
    @ExceptionHandler({MultipartException.class, MissingServletRequestPartException.class})
    public ResponseEntity<String> handleMultipartErrors(Exception e) {
      return ResponseEntity.badRequest().body("Invalid upload request: " + e.getMessage());
    }
  }
}
