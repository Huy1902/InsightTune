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
   * Upload một file MP3 lên Object Store Bucket.
   *
   * @param file file nhạc cần upload
   * @return thông tin file sau khi upload thành công
   */
  @PostMapping("/upload")
  @Operation(summary = "Upload a mp3 file to Object Store Bucket")
  public ResponseEntity<TrackUploadResponseDto> upload(@RequestParam MultipartFile file) {

    TrackUploadResponseDto trackUploadResponseDto = null;
    try {
      trackUploadResponseDto = uploadService.uploadTrack(new TrackUploadRequestDto(file));
    } catch (UploadServiceException e) {
      log.error(e.getMessage());
    }
    return ResponseEntity.ok().body(trackUploadResponseDto);
  }

  /**
   * Xử lý lỗi toàn cục cho các request upload không hợp lệ.
   */
  @RestControllerAdvice
  public static class GlobalExceptionHandler {

    /**
     * Bắt và xử lý lỗi khi request upload bị sai định dạng hoặc thiếu phần dữ liệu.
     *
     * @param e ngoại lệ phát sinh trong quá trình upload
     * @return thông báo lỗi 400 (Bad Request)
     */
    @ExceptionHandler({MultipartException.class, MissingServletRequestPartException.class})
    public ResponseEntity<String> handleMultipartErrors(Exception e) {
      return ResponseEntity.badRequest().body("Invalid upload request: " + e.getMessage());
    }
  }
}
