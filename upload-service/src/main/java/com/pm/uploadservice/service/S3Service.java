package com.pm.uploadservice.service;

import com.pm.uploadservice.dto.S3UploadRequestDto;
import com.pm.uploadservice.dto.S3UploadResponseDto;
import com.pm.uploadservice.exception.S3ServiceException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Set;

/**
 * Service class that handles uploading and downloading files (tracks and cover images)
 * to and from an Amazon S3 bucket.
 *
 * <p>Tracks are stored under the <code>tracks/</code> prefix with an <code>.mp3</code> extension,
 * and cover images (if provided) are stored under the <code>covers/</code> prefix with a
 * <code>.jpeg</code> extension.</p>
 *
 * <p>Configuration:</p>
 * <ul>
 *   <li><b>aws.bucket.name</b> – the target S3 bucket name (injected from application properties)</li>
 * </ul>
 *
 * @author Huy1902
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

  private final S3Client s3Client;
  private final Validator validator;

  @Value("${aws.bucket.name}")
  private String bucketName;

  /**
   * Uploads a track (and optional cover image) to S3.
   *
   * <p>The track is stored under the <code>tracks/</code> prefix with an <code>.mp3</code> extension.
   * If a cover image is provided, it is stored under the <code>covers/</code> prefix with a
   * <code>.jpeg</code> extension.</p>
   *
   * @param s3UploadRequestDto the upload request containing the track file, cover image (optional),
   *                           and unique key used for naming.
   * @return a {@link S3UploadResponseDto} containing the storage keys and metadata.
   */
  public S3UploadResponseDto uploadTrack(S3UploadRequestDto s3UploadRequestDto) {
    String keyTrack = "tracks/" + s3UploadRequestDto.getKey() + ".mp3";

    S3UploadResponseDto.S3UploadResponseDtoBuilder s3UploadResponseDtoBuilder = S3UploadResponseDto.builder()
            .title(s3UploadRequestDto.getFile().getOriginalFilename());

    try {
      s3Client.putObject(
              PutObjectRequest.builder()
                      .bucket(bucketName)
                      .key(keyTrack)
                      .build(),
              RequestBody.fromBytes(s3UploadRequestDto.getFile().getBytes())
      );
      s3UploadResponseDtoBuilder.status("Success");
    } catch (IOException e) {
      log.error("Failed to upload track: {}", e.getMessage());
      throw new S3ServiceException("Failed to upload track: " + e.getMessage());
    }

    s3UploadResponseDtoBuilder.storageKey(keyTrack);

    if (s3UploadRequestDto.getImage() != null) {
      String keyImage = "covers/" + s3UploadRequestDto.getKey() + ".jpeg";
      s3Client.putObject(
              PutObjectRequest.builder()
                      .bucket(bucketName)
                      .key(keyImage)
                      .build(),
              RequestBody.fromBytes(s3UploadRequestDto.getImage())
      );
      s3UploadResponseDtoBuilder.coverImageKey(keyImage);
    }

    S3UploadResponseDto s3UploadResponseDto = s3UploadResponseDtoBuilder.build();

    Set<ConstraintViolation<S3UploadResponseDto>> violations = validator.validate(s3UploadResponseDto);
    if (!violations.isEmpty()) {
      throw new S3ServiceException(violations.iterator().next().getMessage());
    }

    return s3UploadResponseDto;
  }

  /**
   * Downloads a track from S3.
   *
   * <p>The method looks under the <code>tracks/</code> prefix and retrieves the file
   * as a byte array.</p>
   *
   * @param key the key (without prefix or extension) identifying the track in S3.
   * @return the file contents as a byte array.
   */
  public byte[] downloadFile(String key) {
    ResponseBytes<GetObjectResponse> objectAsBytes =
            s3Client.getObjectAsBytes(GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key("tracks/" + key)
                    .build());
    return objectAsBytes.asByteArray();
  }
}
