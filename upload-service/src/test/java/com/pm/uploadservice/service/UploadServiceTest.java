package com.pm.uploadservice.service;

import com.pm.uploadservice.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadServiceTest {

  @Mock private MetadataService metadataService;
  @Mock private S3Service s3Service;
  @Mock private KafkaService kafkaService;

  @InjectMocks private UploadService uploadService;

  @Test
  void givenValidMp3_whenUploadTrack_thenMetadataS3KafkaCalledAndResponseAssembled() throws Exception {
    // -------- given
    // incoming file (content itself doesn't matter for this unit test)
    var mp3 = new MockMultipartFile("file", "song.mp3", "audio/mpeg", "fake-mp3".getBytes(StandardCharsets.UTF_8));
    var request = new TrackUploadRequestDto(mp3);

    // metadata returned by MetadataService (note spaces to exercise trim())
    var meta = MetaResponseDto.builder().build();
    meta.setTitle("Love Story");
    meta.setAlbum("Fearless");
    meta.setDurationMs(230_000);
    meta.setArtists(List.of("  Taylor Swift  ", "Feat. Someone"));
    byte[] imageBytes = "img".getBytes(StandardCharsets.UTF_8);
    meta.setImage(imageBytes);
    meta.setImageType("image/jpeg");

    when(metadataService.buildMetadata(any(MetaRequestDto.class))).thenReturn(meta);

    // S3 upload result
    var s3Resp = S3UploadResponseDto.builder().build();
    s3Resp.setStorageKey("tracks/abc123.mp3");
    s3Resp.setCoverImageKey("covers/abc123.jpg");
    s3Resp.setStatus("OK");
    when(s3Service.uploadTrack(any(S3UploadRequestDto.class))).thenReturn(s3Resp);

    // Kafka send result
    var kafkaResp = new CreatedTrackResponseDto("SENT");
    when(kafkaService.sendCreatedTrack(any(CreatedTrackRequestDto.class))).thenReturn(kafkaResp);

    // Pre-compute expected baseKey with the same algorithm as the service
    String rawKey = "Taylor Swift" + "Love Story"; // trimmed concat
    String expectedBaseKey = computeBaseKey(rawKey);

    // -------- when
    TrackUploadResponseDto result = uploadService.uploadTrack(request);

    // -------- then
    // verify S3 called with computed baseKey and correct file/image
    ArgumentCaptor<S3UploadRequestDto> s3ReqCap = ArgumentCaptor.forClass(S3UploadRequestDto.class);
    verify(s3Service, times(1)).uploadTrack(s3ReqCap.capture());
    S3UploadRequestDto s3Req = s3ReqCap.getValue();
    assertThat(s3Req.getKey()).isEqualTo(expectedBaseKey);
    assertThat(s3Req.getFile()).isSameAs(mp3);
    assertThat(s3Req.getImage()).isEqualTo(imageBytes);
    assertThat(s3Req.getImageType()).isEqualTo("image/jpeg");

    // verify Kafka called with metadata + S3 keys
    ArgumentCaptor<CreatedTrackRequestDto> kafkaReqCap = ArgumentCaptor.forClass(CreatedTrackRequestDto.class);
    verify(kafkaService, times(1)).sendCreatedTrack(kafkaReqCap.capture());
    CreatedTrackRequestDto createdReq = kafkaReqCap.getValue();
    assertThat(createdReq.getTitle()).isEqualTo("  Love Story  ".trim());
    assertThat(createdReq.getAlbum()).isEqualTo("Fearless");
    assertThat(createdReq.getDurationMs()).isEqualTo(230_000);
    assertThat(createdReq.getStorageKey()).isEqualTo("tracks/abc123.mp3");
    assertThat(createdReq.getCoverImageKey()).isEqualTo("covers/abc123.jpg");
    assertThat(createdReq.getArtists()).containsExactly("  Taylor Swift  ", "Feat. Someone"); // service trims only for key, not for Kafka

    // final response fields
    assertThat(result.getTitle()).isEqualTo("Love Story");
    assertThat(result.getArtists()).containsExactly("  Taylor Swift  ", "Feat. Someone");
    assertThat(result.getStorageKey()).isEqualTo("tracks/abc123.mp3");
    assertThat(result.getCoverImageKey()).isEqualTo("covers/abc123.jpg");
    assertThat(result.getKafkaStatus()).isEqualTo("SENT");
    assertThat(result.getS3Status()).isEqualTo("OK");

    verify(metadataService, times(1)).buildMetadata(any(MetaRequestDto.class));
    verifyNoMoreInteractions(metadataService, s3Service, kafkaService);
  }

  @Test
  void givenMetadataThrows_whenUploadTrack_thenCurrentCodeNpeBecauseMetaIsNull() throws Exception {
    // This documents current behavior: service logs and then dereferences metaResponseDto (null)
    var mp3 = new MockMultipartFile("file", "bad.mp3", "audio/mpeg", new byte[]{});
    var request = new TrackUploadRequestDto(mp3);

    when(metadataService.buildMetadata(any(MetaRequestDto.class)))
            .thenThrow(new IOException("broken file"));

    assertThatThrownBy(() -> uploadService.uploadTrack(request))
            .isInstanceOf(NullPointerException.class);

    verify(metadataService, times(1)).buildMetadata(any(MetaRequestDto.class));
    verifyNoInteractions(s3Service, kafkaService);
  }

  // Helper to reproduce baseKey algorithm
  private static String computeBaseKey(String rawKey) throws NoSuchAlgorithmException {
    byte[] digest = MessageDigest.getInstance("SHA-256").digest(rawKey.getBytes(StandardCharsets.UTF_8));
    long value = new BigInteger(1, Arrays.copyOf(digest, 8)).longValue();
    return String.format("%012d", value);
  }
}
