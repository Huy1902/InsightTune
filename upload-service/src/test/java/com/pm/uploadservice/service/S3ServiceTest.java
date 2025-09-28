package com.pm.uploadservice.service;

import com.pm.uploadservice.dto.S3UploadRequestDto;
import com.pm.uploadservice.dto.S3UploadResponseDto;
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
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

  @Mock
  S3Client s3Client;

  @InjectMocks
  S3Service s3Service;

  @Test
  void givenValidTrackAndImage_whenUploadTrack_thenUploadsBothAndReturnsSuccess() throws Exception {
    // Arrange
    ReflectionTestUtils.setField(s3Service, "bucketName", "my-bucket");

    var file = new MockMultipartFile(
            "file", "song.mp3", "audio/mpeg", "fake-mp3".getBytes(StandardCharsets.UTF_8));
    byte[] image = "jpeg".getBytes(StandardCharsets.UTF_8);

    var req = S3UploadRequestDto.builder()
            .file(file)
            .image(image)
            .imageType("image/jpeg")
            .key("000123456789")
            .build();

    when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenReturn(PutObjectResponse.builder().build());

    // Act
    S3UploadResponseDto resp = s3Service.uploadTrack(req);

    // Assert
    assertThat(resp.getStatus()).isEqualTo("Success");
    assertThat(resp.getStorageKey()).isEqualTo("tracks/000123456789.mp3");
    assertThat(resp.getCoverImageKey()).isEqualTo("covers/000123456789.jpeg");
    assertThat(resp.getTitle()).isEqualTo("song.mp3");

    // Verify track upload call
    ArgumentCaptor<PutObjectRequest> trackReqCap = ArgumentCaptor.forClass(PutObjectRequest.class);
    verify(s3Client, atLeastOnce()).putObject(trackReqCap.capture(), any(RequestBody.class));
    PutObjectRequest firstPut = trackReqCap.getAllValues().get(0);
    assertThat(firstPut.bucket()).isEqualTo("my-bucket");
    assertThat(firstPut.key()).isEqualTo("tracks/000123456789.mp3");

    // Verify image upload call
    PutObjectRequest secondPut = trackReqCap.getAllValues().get(1);
    assertThat(secondPut.bucket()).isEqualTo("my-bucket");
    assertThat(secondPut.key()).isEqualTo("covers/000123456789.jpeg");

    verifyNoMoreInteractions(s3Client);
  }

  @Test
  void givenTrackBytesIOException_whenUploadTrack_thenStatusFailedButImageStillUploadedIfPresent() throws Exception {
    // Arrange
    ReflectionTestUtils.setField(s3Service, "bucketName", "bucket-x");

    MultipartFile brokenFile = mock(MultipartFile.class);
    when(brokenFile.getOriginalFilename()).thenReturn("broken.mp3");
    when(brokenFile.getBytes()).thenThrow(new IOException("boom")); // triggers catch -> "Failed"

    byte[] image = new byte[]{1, 2, 3};

    var req = S3UploadRequestDto.builder()
            .file(brokenFile)
            .image(image)
            .imageType("image/jpeg")
            .key("k123")
            .build();

    when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenReturn(PutObjectResponse.builder().build());

    // Act
    S3UploadResponseDto resp = s3Service.uploadTrack(req);

    // Assert
    assertThat(resp.getStatus()).isEqualTo("Failed"); // from catch block
    assertThat(resp.getStorageKey()).isEqualTo("tracks/k123.mp3");
    assertThat(resp.getCoverImageKey()).isEqualTo("covers/k123.jpeg"); // image still attempted
    assertThat(resp.getTitle()).isEqualTo("broken.mp3");

    // Verify: track putObject should NOT be invoked because getBytes() threw before the call
    // But image putObject SHOULD be invoked
    ArgumentCaptor<PutObjectRequest> reqCap = ArgumentCaptor.forClass(PutObjectRequest.class);
    verify(s3Client, times(1)).putObject(reqCap.capture(), any(RequestBody.class));
    assertThat(reqCap.getValue().key()).isEqualTo("covers/k123.jpeg");
    assertThat(reqCap.getValue().bucket()).isEqualTo("bucket-x");

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
    byte[] actual = s3Service.downloadFile("k999.mp3"); // service prefixes "tracks/"

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
    // given
    MockMultipartFile trackFile = new MockMultipartFile(
            "file", "song.mp3", "audio/mpeg", "dummy-bytes".getBytes()
    );

    S3UploadRequestDto request = S3UploadRequestDto.builder()
            .file(trackFile)
            .image(null)  // no cover image
            .key("123456789012")
            .build();

    // when
    S3UploadResponseDto response = s3Service.uploadTrack(request);

    // then: track uploaded
    ArgumentCaptor<PutObjectRequest> putCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
    verify(s3Client, times(1))
            .putObject(putCaptor.capture(), any(RequestBody.class));

    PutObjectRequest trackPut = putCaptor.getValue();
    assertThat(trackPut.key()).isEqualTo("tracks/123456789012.mp3");

    // response assertions
    assertThat(response.getStorageKey()).isEqualTo("tracks/123456789012.mp3");
    assertThat(response.getCoverImageKey()).isNull(); // no image uploaded
    assertThat(response.getStatus()).isEqualTo("Success");

    // verify no other interactions (no second putObject for image)
    verifyNoMoreInteractions(s3Client);
  }
}
