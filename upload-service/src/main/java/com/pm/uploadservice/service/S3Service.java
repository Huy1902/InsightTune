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
 * Lớp dịch vụ xử lý việc tải lên và tải xuống file (bài hát và ảnh bìa)
 * từ/đến một bucket Amazon S3.
 *
 * <p>Các bài hát được lưu dưới tiền tố <code>tracks/</code> với phần mở rộng <code>.mp3</code>,
 * và ảnh bìa (nếu có) được lưu dưới tiền tố <code>covers/</code> với phần mở rộng
 * <code>.jpeg</code>.</p>
 *
 * <p>Cấu hình:</p>
 * <ul>
 *   <li><b>aws.bucket.name</b> – tên bucket S3 đích (được inject từ application properties)</li>
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
   * Tải lên một bài hát (và ảnh bìa tùy chọn) lên S3.
   *
   * <p>Bài hát được lưu dưới tiền tố <code>tracks/</code> với phần mở rộng <code>.mp3</code>.
   * Nếu cung cấp ảnh bìa, ảnh sẽ được lưu dưới tiền tố <code>covers/</code> với phần mở rộng
   * <code>.jpeg</code>.</p>
   *
   * @param s3UploadRequestDto đối tượng yêu cầu upload chứa file bài hát, ảnh bìa (tùy chọn),
   *                           và key duy nhất dùng để đặt tên.
   * @return một {@link S3UploadResponseDto} chứa key lưu trữ và metadata.
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
   * Tải xuống một bài hát từ S3.
   *
   * <p>Phương thức tìm dưới tiền tố <code>tracks/</code> và lấy file
   * dưới dạng mảng byte.</p>
   *
   * @param key key (không bao gồm tiền tố hoặc phần mở rộng) xác định bài hát trong S3.
   * @return nội dung file dưới dạng mảng byte.
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
