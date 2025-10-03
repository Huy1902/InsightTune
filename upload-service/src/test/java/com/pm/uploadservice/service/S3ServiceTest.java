package com.pm.uploadservice.service;

import com.pm.uploadservice.dto.S3UploadRequestDto;
import com.pm.uploadservice.dto.S3UploadResponseDto;
import com.pm.uploadservice.exception.S3ServiceException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

  @Mock
  S3Client s3Client;

  @Mock
  Validator validator;

  @InjectMocks
  S3Service s3Service;

  @Test
  void givenValidTrackAndImage_whenUploadTrack_thenUploadsBothAndReturnsSuccess() throws Exception {
    // Arrange
    ReflectionTestUtils.setField(s3Service, "bucketName", "my-bucket");
    when(validator.validate(any())).thenReturn(Collections.emptySet());
    when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenReturn(PutObjectResponse.builder().build());

    var file = new MockMultipartFile(
            "file", "song.mp3", "audio/mpeg", "fake-mp3".getBytes(StandardCharsets.UTF_8));
    byte[] image = "jpeg".getBytes(StandardCharsets.UTF_8);

    var req = S3UploadRequestDto.builder()
            .file(file)
            .image(image)
            .imageType("image/jpeg")
            .key("000123456789")
            .build();

    // Act
    S3UploadResponseDto resp = s3Service.uploadTrack(req);

    // Assert
    assertThat(resp.getStatus()).isEqualTo("Success");
    assertThat(resp.getStorageKey()).isEqualTo("tracks/000123456789.mp3");
    assertThat(resp.getCoverImageKey()).isEqualTo("covers/000123456789.jpeg");
    assertThat(resp.getTitle()).isEqualTo("song.mp3");

    ArgumentCaptor<PutObjectRequest> putCap = ArgumentCaptor.forClass(PutObjectRequest.class);
    verify(s3Client, times(2)).putObject(putCap.capture(), any(RequestBody.class));

    PutObjectRequest firstPut = putCap.getAllValues().get(0);
    assertThat(firstPut.bucket()).isEqualTo("my-bucket");
    assertThat(firstPut.key()).isEqualTo("tracks/000123456789.mp3");

    PutObjectRequest secondPut = putCap.getAllValues().get(1);
    assertThat(secondPut.bucket()).isEqualTo("my-bucket");
    assertThat(secondPut.key()).isEqualTo("covers/000123456789.jpeg");

    verifyNoMoreInteractions(s3Client);
  }

  @Test
  void givenTrackBytesIOException_whenUploadTrack_thenThrowsAndNoUploadHappens() throws Exception {
    ReflectionTestUtils.setField(s3Service, "bucketName", "bucket-x");

    MultipartFile brokenFile = mock(MultipartFile.class);
    when(brokenFile.getOriginalFilename()).thenReturn("broken.mp3");
    when(brokenFile.getBytes()).thenThrow(new IOException("boom"));

    byte[] image = new byte[]{1, 2, 3};

    var req = S3UploadRequestDto.builder()
            .file(brokenFile)
            .image(image)
            .imageType("image/jpeg")
            .key("k123")
            .build();

    // No stubbing for validator here — it's never reached

    assertThatThrownBy(() -> s3Service.uploadTrack(req))
            .isInstanceOf(S3ServiceException.class)
            .hasMessageContaining("Failed to upload track");

    verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    verifyNoMoreInteractions(s3Client);
  }


  @Test
  void givenExistingObject_whenDownloadFile_thenReturnsBytes() {
    // Arrange
    ReflectionTestUtils.setField(s3Service, "bucketName", "bkt");

    byte[] expected = "hello".getBytes(StandardCharsets.UTF_8);
    ResponseBytes<GetObjectResponse> responseBytes =
            ResponseBytes.fromByteArray(GetObjectResponse.builder().build(), expected);
    when(s3Client.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(responseBytes);

    // Act
    byte[] actual = s3Service.downloadFile("k999.mp3");

    // Assert
    assertThat(actual).isEqualTo(expected);

    ArgumentCaptor<GetObjectRequest> getReqCap = ArgumentCaptor.forClass(GetObjectRequest.class);
    verify(s3Client).getObjectAsBytes(getReqCap.capture());
    GetObjectRequest go = getReqCap.getValue();
    assertThat(go.bucket()).isEqualTo("bkt");
    assertThat(go.key()).isEqualTo("tracks/k999.mp3");

    verifyNoMoreInteractions(s3Client);
  }

  @Test
  void givenImageNull_whenUploadTrack_thenOnlyTrackIsUploaded() throws Exception {
    // Arrange
    ReflectionTestUtils.setField(s3Service, "bucketName", "my-bucket");
    when(validator.validate(any())).thenReturn(Collections.emptySet());

    MockMultipartFile trackFile = new MockMultipartFile(
            "file", "song.mp3", "audio/mpeg", "dummy-bytes".getBytes()
    );

    S3UploadRequestDto request = S3UploadRequestDto.builder()
            .file(trackFile)
            .image(null)
            .key("123456789012")
            .build();

    when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenReturn(PutObjectResponse.builder().build());

    // Act
    S3UploadResponseDto response = s3Service.uploadTrack(request);

    // Assert
    ArgumentCaptor<PutObjectRequest> putCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
    verify(s3Client, times(1)).putObject(putCaptor.capture(), any(RequestBody.class));

    PutObjectRequest trackPut = putCaptor.getValue();
    assertThat(trackPut.bucket()).isEqualTo("my-bucket");
    assertThat(trackPut.key()).isEqualTo("tracks/123456789012.mp3");

    assertThat(response.getStorageKey()).isEqualTo("tracks/123456789012.mp3");
    assertThat(response.getCoverImageKey()).isNull();
    assertThat(response.getStatus()).isEqualTo("Success");

    verifyNoMoreInteractions(s3Client);
  }

  @Test
  void givenValidTrackAndImage_butValidatorViolation_whenUploadTrack_thenThrowsAndUploadsStillHappened() throws Exception {
    // Arrange
    ReflectionTestUtils.setField(s3Service, "bucketName", "my-bucket");

    var file = new MockMultipartFile(
            "file", "song.mp3", "audio/mpeg", "bytes".getBytes()
    );
    byte[] image = new byte[]{9, 9, 9};

    var req = S3UploadRequestDto.builder()
            .file(file)
            .image(image)
            .imageType("image/jpeg")
            .key("abc123")
            .build();

    // S3 accepts both uploads
    when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenReturn(PutObjectResponse.builder().build());

    // Validator returns a violation
    @SuppressWarnings("unchecked")
    ConstraintViolation<S3UploadResponseDto> violation = mock(ConstraintViolation.class);
    when(violation.getMessage()).thenReturn("title must not be blank");
    when(validator.validate(any(S3UploadResponseDto.class)))
            .thenReturn(Set.of(violation));

    // Act + Assert
    assertThatThrownBy(() -> s3Service.uploadTrack(req))
            .isInstanceOf(S3ServiceException.class)
            .hasMessage("title must not be blank");

    // Side-effects: both putObject calls happened (track then cover)
    ArgumentCaptor<PutObjectRequest> putCap = ArgumentCaptor.forClass(PutObjectRequest.class);
    verify(s3Client, times(2)).putObject(putCap.capture(), any(RequestBody.class));

    PutObjectRequest trackPut = putCap.getAllValues().get(0);
    assertThat(trackPut.bucket()).isEqualTo("my-bucket");
    assertThat(trackPut.key()).isEqualTo("tracks/abc123.mp3");

    PutObjectRequest coverPut = putCap.getAllValues().get(1);
    assertThat(coverPut.bucket()).isEqualTo("my-bucket");
    assertThat(coverPut.key()).isEqualTo("covers/abc123.jpeg");

    verifyNoMoreInteractions(s3Client);
  }

  @Test
  void givenTrackOnly_butValidatorViolation_whenUploadTrack_thenThrowsAfterTrackUploaded() throws Exception {
    // Arrange
    ReflectionTestUtils.setField(s3Service, "bucketName", "bucket-x");

    var file = new MockMultipartFile(
            "file", "solo.mp3", "audio/mpeg", "bytes".getBytes()
    );

    var req = S3UploadRequestDto.builder()
            .file(file)
            .image(null)  // no cover
            .key("k-only")
            .build();

    when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenReturn(PutObjectResponse.builder().build());

    @SuppressWarnings("unchecked")
    ConstraintViolation<S3UploadResponseDto> violation = mock(ConstraintViolation.class);
    when(violation.getMessage()).thenReturn("storageKey invalid");
    when(validator.validate(any(S3UploadResponseDto.class)))
            .thenReturn(Set.of(violation));

    // Act + Assert
    assertThatThrownBy(() -> s3Service.uploadTrack(req))
            .isInstanceOf(S3ServiceException.class)
            .hasMessage("storageKey invalid");

    // Only one upload (track) should have happened
    ArgumentCaptor<PutObjectRequest> putCap = ArgumentCaptor.forClass(PutObjectRequest.class);
    verify(s3Client, times(1)).putObject(putCap.capture(), any(RequestBody.class));
    assertThat(putCap.getValue().bucket()).isEqualTo("bucket-x");
    assertThat(putCap.getValue().key()).isEqualTo("tracks/k-only.mp3");

    verifyNoMoreInteractions(s3Client);
  }
}
