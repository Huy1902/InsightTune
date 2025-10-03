package com.pm.uploadservice.service;

import com.pm.uploadservice.dto.*;
import com.pm.uploadservice.exception.KafkaServiceException;
import com.pm.uploadservice.exception.MetaExtractException;
import com.pm.uploadservice.exception.S3ServiceException;
import com.pm.uploadservice.exception.UploadServiceException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadServiceTest {

  @Mock
  private MetadataService metadataService;
  @Mock
  private S3Service s3Service;
  @Mock
  private KafkaService kafkaService;
  @Mock
  private Validator validator;

  @InjectMocks
  private UploadService uploadService;

  // ---------- Helpers

  private static MetaResponseDto sampleMeta() {
    MetaResponseDto meta = MetaResponseDto.builder().build();
    meta.setTitle("Love Story");
    meta.setAlbum("Fearless");
    meta.setDurationMs(230_000);
    meta.setArtists(List.of("  Taylor Swift  ", "Feat. Someone"));
    meta.setImage("img".getBytes(StandardCharsets.UTF_8));
    meta.setImageType("image/jpeg");
    return meta;
  }

  private static String computeBaseKey(String rawKey) throws NoSuchAlgorithmException {
    byte[] digest = MessageDigest.getInstance("SHA-256").digest(rawKey.getBytes(StandardCharsets.UTF_8));
    long value = new BigInteger(1, Arrays.copyOf(digest, 8)).longValue();
    return String.format("%012d", value);
  }

  // ===================================================================================
  // Success path: metadata ok -> S3 validated & uploaded -> Kafka validated & sent -> final response validated
  // ===================================================================================
  @Test
  void givenValidMp3_whenUploadTrack_thenMetadataS3KafkaCalledAndResponseAssembled() throws Exception {
    // given
    var mp3 = new MockMultipartFile("file", "song.mp3", "audio/mpeg", "fake".getBytes());
    var request = new TrackUploadRequestDto(mp3);

    when(metadataService.buildMetadata(any(MetaRequestDto.class))).thenReturn(sampleMeta());

    // One routed validator stub for this test: everything passes
    when(validator.validate(any())).thenAnswer(inv -> Collections.emptySet());

    var s3Resp = S3UploadResponseDto.builder().build();
    s3Resp.setStorageKey("tracks/abc123.mp3");
    s3Resp.setCoverImageKey("covers/abc123.jpg");
    s3Resp.setStatus("OK");
    when(s3Service.uploadTrack(any(S3UploadRequestDto.class))).thenReturn(s3Resp);

    var kafkaResp = new CreatedTrackResponseDto("SENT");
    when(kafkaService.sendCreatedTrack(any(CreatedTrackRequestDto.class))).thenReturn(kafkaResp);

    // baseKey is computed from first artist (trim) + title (trim)
    String expectedBaseKey = computeBaseKey("Taylor Swift" + "Love Story");

    // when
    TrackUploadResponseDto result = uploadService.uploadTrack(request);

    // then: S3 request captured
    ArgumentCaptor<S3UploadRequestDto> s3ReqCap = ArgumentCaptor.forClass(S3UploadRequestDto.class);
    verify(s3Service).uploadTrack(s3ReqCap.capture());
    S3UploadRequestDto s3Req = s3ReqCap.getValue();
    assertThat(s3Req.getKey()).isEqualTo(expectedBaseKey);
    assertThat(s3Req.getFile()).isSameAs(mp3);
    assertThat(s3Req.getImage()).isNotNull();
    assertThat(s3Req.getImageType()).isEqualTo("image/jpeg");

    // Kafka request captured
    ArgumentCaptor<CreatedTrackRequestDto> kReqCap = ArgumentCaptor.forClass(CreatedTrackRequestDto.class);
    verify(kafkaService).sendCreatedTrack(kReqCap.capture());
    CreatedTrackRequestDto kReq = kReqCap.getValue();
    assertThat(kReq.getTitle()).isEqualTo("Love Story");
    assertThat(kReq.getAlbum()).isEqualTo("Fearless");
    assertThat(kReq.getDurationMs()).isEqualTo(230_000);
    assertThat(kReq.getStorageKey()).isEqualTo("tracks/abc123.mp3");
    assertThat(kReq.getCoverImageKey()).isEqualTo("covers/abc123.jpg");
    // artists are not trimmed by UploadService (only used trimmed for key)
    assertThat(kReq.getArtists()).containsExactly("  Taylor Swift  ", "Feat. Someone");

    // Final response
    assertThat(result.getTitle()).isEqualTo("Love Story");
    assertThat(result.getArtists()).containsExactly("  Taylor Swift  ", "Feat. Someone");
    assertThat(result.getStorageKey()).isEqualTo("tracks/abc123.mp3");
    assertThat(result.getCoverImageKey()).isEqualTo("covers/abc123.jpg");
    assertThat(result.getKafkaStatus()).isEqualTo("SENT");
    assertThat(result.getS3Status()).isEqualTo("OK");

    verifyNoMoreInteractions(metadataService, s3Service, kafkaService);
  }

  // ===================================================================================
  // Metadata fails -> UploadServiceException (rethrow) and no downstream calls
  // ===================================================================================
  @Test
  void givenMetadataFails_whenUploadTrack_thenThrowsUploadServiceException_andNoDownstreamCalls() throws Exception {
    var mp3 = new MockMultipartFile("file", "bad.mp3", "audio/mpeg", new byte[]{1});
    var request = new TrackUploadRequestDto(mp3);

    when(metadataService.buildMetadata(any(MetaRequestDto.class)))
            .thenThrow(new MetaExtractException("bad meta"));

    assertThatThrownBy(() -> uploadService.uploadTrack(request))
            .isInstanceOf(UploadServiceException.class)
            .hasMessage("bad meta");

    verify(metadataService).buildMetadata(any(MetaRequestDto.class));
    verifyNoInteractions(s3Service, kafkaService);
  }

  // ===================================================================================
  // S3 upload throws -> wrapped as UploadServiceException; Kafka not called
  // ===================================================================================
  @Test
  void givenS3UploadThrows_whenUploadTrack_thenWrappedAsUploadServiceException_andKafkaNotCalled() throws Exception {
    var mp3 = new MockMultipartFile("file", "song.mp3", "audio/mpeg", new byte[]{1});
    var request = new TrackUploadRequestDto(mp3);

    when(metadataService.buildMetadata(any(MetaRequestDto.class))).thenReturn(sampleMeta());
    when(validator.validate(any())).thenAnswer(inv -> Collections.emptySet());

    when(s3Service.uploadTrack(any(S3UploadRequestDto.class)))
            .thenThrow(new S3ServiceException("s3 failed"));

    assertThatThrownBy(() -> uploadService.uploadTrack(request))
            .isInstanceOf(UploadServiceException.class)
            .hasMessage("s3 failed");

    verify(s3Service).uploadTrack(any(S3UploadRequestDto.class));
    verifyNoInteractions(kafkaService);
  }

  // ===================================================================================
  // Kafka send throws -> wrapped as UploadServiceException; S3 did happen
  // ===================================================================================
  @Test
  void givenKafkaSendThrows_whenUploadTrack_thenWrappedAsUploadServiceException_afterS3Succeeded() throws Exception {
    var mp3 = new MockMultipartFile("file", "song.mp3", "audio/mpeg", new byte[]{1});
    var request = new TrackUploadRequestDto(mp3);

    when(metadataService.buildMetadata(any(MetaRequestDto.class))).thenReturn(sampleMeta());

    when(validator.validate(any())).thenAnswer(inv -> Collections.emptySet());

    var s3Resp = S3UploadResponseDto.builder().build();
    s3Resp.setStorageKey("tracks/x.mp3");
    s3Resp.setCoverImageKey("covers/x.jpg");
    s3Resp.setStatus("OK");
    when(s3Service.uploadTrack(any(S3UploadRequestDto.class))).thenReturn(s3Resp);

    when(kafkaService.sendCreatedTrack(any(CreatedTrackRequestDto.class)))
            .thenThrow(new KafkaServiceException("kafka down"));

    assertThatThrownBy(() -> uploadService.uploadTrack(request))
            .isInstanceOf(UploadServiceException.class)
            .hasMessage("kafka down");

    verify(s3Service).uploadTrack(any(S3UploadRequestDto.class));
    verify(kafkaService).sendCreatedTrack(any(CreatedTrackRequestDto.class));
  }

  // ===================================================================================
  // Validator fails for S3 request -> throws before S3 call
  // (Use one routed stub to avoid unfinished stubbing and generics issues)
  // ===================================================================================
  @Test
  void givenValidatorFailsForS3Request_whenUploadTrack_thenThrowsBeforeCallingS3() throws Exception {
    var mp3 = new MockMultipartFile("file", "song.mp3", "audio/mpeg", new byte[]{1});
    var request = new TrackUploadRequestDto(mp3);

    when(metadataService.buildMetadata(any(MetaRequestDto.class))).thenReturn(sampleMeta());

    @SuppressWarnings("unchecked")
    ConstraintViolation<S3UploadRequestDto> s3V = mock(ConstraintViolation.class);
    when(s3V.getMessage()).thenReturn("invalid s3 request");

    // Single routed validator stub (NO broad + typed stubs together)
    when(validator.validate(any())).thenAnswer(inv -> {
      Object arg = inv.getArgument(0);
      if (arg instanceof S3UploadRequestDto) return Set.of(s3V);               // fail here
      if (arg instanceof CreatedTrackRequestDto) return Collections.emptySet();
      if (arg instanceof TrackUploadResponseDto) return Collections.emptySet();
      return Collections.emptySet();
    });

    assertThatThrownBy(() -> uploadService.uploadTrack(request))
            .isInstanceOf(UploadServiceException.class)
            .hasMessage("invalid s3 request");

    verifyNoInteractions(s3Service, kafkaService);
  }

  // ===================================================================================
  // Validator fails for Kafka request -> S3 is called (passes), then throw before Kafka send
  // ===================================================================================
  @Test
  void givenValidatorFailsForKafkaRequest_whenUploadTrack_thenThrowsBeforeCallingKafka() throws Exception {
    var mp3 = new MockMultipartFile("file", "song.mp3", "audio/mpeg", new byte[]{1});
    var request = new TrackUploadRequestDto(mp3);

    when(metadataService.buildMetadata(any(MetaRequestDto.class))).thenReturn(sampleMeta());

    var s3Resp = S3UploadResponseDto.builder().build();
    s3Resp.setStorageKey("tracks/y.mp3");
    s3Resp.setCoverImageKey("covers/y.jpg");
    s3Resp.setStatus("OK");
    when(s3Service.uploadTrack(any(S3UploadRequestDto.class))).thenReturn(s3Resp);

    @SuppressWarnings("unchecked")
    ConstraintViolation<CreatedTrackRequestDto> kafkaV = mock(ConstraintViolation.class);
    when(kafkaV.getMessage()).thenReturn("invalid kafka request");

    when(validator.validate(any())).thenAnswer(inv -> {
      Object arg = inv.getArgument(0);
      if (arg instanceof S3UploadRequestDto) return Collections.emptySet();     // allow S3
      if (arg instanceof CreatedTrackRequestDto) return Set.of(kafkaV);            // fail here
      if (arg instanceof TrackUploadResponseDto) return Collections.emptySet();
      return Collections.emptySet();
    });

    assertThatThrownBy(() -> uploadService.uploadTrack(request))
            .isInstanceOf(UploadServiceException.class)
            .hasMessage("invalid kafka request");

    verify(s3Service, times(1)).uploadTrack(any(S3UploadRequestDto.class));
    verifyNoInteractions(kafkaService);
  }

  // ===================================================================================
  // Final response validation fails -> S3 & Kafka were called; then throw
  // ===================================================================================
  @Test
  void givenValidatorFailsForFinalResponse_whenUploadTrack_thenThrowsAfterSideEffects() throws Exception {
    var mp3 = new MockMultipartFile("file", "song.mp3", "audio/mpeg", new byte[]{1});
    var request = new TrackUploadRequestDto(mp3);

    when(metadataService.buildMetadata(any(MetaRequestDto.class))).thenReturn(sampleMeta());

    var s3Resp = S3UploadResponseDto.builder().build();
    s3Resp.setStorageKey("tracks/z.mp3");
    s3Resp.setCoverImageKey("covers/z.jpg");
    s3Resp.setStatus("OK");
    when(s3Service.uploadTrack(any(S3UploadRequestDto.class))).thenReturn(s3Resp);

    when(kafkaService.sendCreatedTrack(any(CreatedTrackRequestDto.class)))
            .thenReturn(new CreatedTrackResponseDto("SENT"));

    @SuppressWarnings("unchecked")
    ConstraintViolation<TrackUploadResponseDto> respV = mock(ConstraintViolation.class);
    when(respV.getMessage()).thenReturn("response invalid");

    when(validator.validate(any())).thenAnswer(inv -> {
      Object arg = inv.getArgument(0);
      if (arg instanceof S3UploadRequestDto) return Collections.emptySet();
      if (arg instanceof CreatedTrackRequestDto) return Collections.emptySet();
      if (arg instanceof TrackUploadResponseDto) return Set.of(respV);             // fail here
      return Collections.emptySet();
    });

    assertThatThrownBy(() -> uploadService.uploadTrack(request))
            .isInstanceOf(UploadServiceException.class)
            .hasMessage("response invalid");

    verify(s3Service).uploadTrack(any(S3UploadRequestDto.class));
    verify(kafkaService).sendCreatedTrack(any(CreatedTrackRequestDto.class));
  }
}
