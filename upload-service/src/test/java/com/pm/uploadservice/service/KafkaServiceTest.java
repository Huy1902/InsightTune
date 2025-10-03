package com.pm.uploadservice.service;

import com.pm.uploadservice.dto.CreatedTrackRequestDto;
import com.pm.uploadservice.dto.CreatedTrackResponseDto;
import com.pm.uploadservice.exception.KafkaServiceException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import track.events.CreatedTrackEvent;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaServiceTest {

  @Mock
  KafkaTemplate<String, byte[]> kafkaTemplate;

  @Mock
  Validator validator;

  @InjectMocks
  KafkaService kafkaService;

  private static CreatedTrackRequestDto sampleRequest() {
    var req = CreatedTrackRequestDto.builder().build();
    req.setTitle("Love Story");
    req.setAlbum("Fearless");
    req.setArtists(List.of("Taylor Swift", "Guest"));
    req.setDurationMs(230_000);
    req.setCoverImageKey("covers/abc123.jpg");
    req.setStorageKey("tracks/abc123.mp3");
    return req;
  }

  @Test
  void givenValidRequest_whenSendCreatedTrack_thenBuildsEventSendsToKafkaAndReturnsSuccess() throws Exception {
    // given
    var req = sampleRequest();
    when(validator.validate(any(CreatedTrackEvent.class))).thenReturn(Collections.emptySet());
    // No need to stub kafkaTemplate.send default behavior

    // when
    CreatedTrackResponseDto resp = kafkaService.sendCreatedTrack(req);

    // then
    assertThat(resp.getKafkaStatus()).isEqualTo("Successfully sent event");

    // capture payload
    ArgumentCaptor<byte[]> bytesCaptor = ArgumentCaptor.forClass(byte[].class);
    verify(kafkaTemplate, times(1)).send(eq("created_track"), bytesCaptor.capture());

    CreatedTrackEvent event = CreatedTrackEvent.parseFrom(bytesCaptor.getValue());
    assertThat(event.getTitle()).isEqualTo("Love Story");
    assertThat(event.getAlbum()).isEqualTo("Fearless");
    assertThat(event.getArtistsList()).containsExactly("Taylor Swift", "Guest");
    assertThat(event.getDurationMs()).isEqualTo(230_000);
    assertThat(event.getCoverImageKey()).isEqualTo("covers/abc123.jpg");
    assertThat(event.getStorageKey()).isEqualTo("tracks/abc123.mp3");

    verifyNoMoreInteractions(kafkaTemplate);
  }

  @Test
  void givenKafkaTemplateThrows_whenSendCreatedTrack_thenThrowsKafkaServiceException() {
    // given
    var req = sampleRequest();
    when(validator.validate(any(CreatedTrackEvent.class))).thenReturn(Collections.emptySet());
    when(kafkaTemplate.send(eq("created_track"), any(byte[].class)))
            .thenThrow(new RuntimeException("boom"));

    // when + then
    assertThatThrownBy(() -> kafkaService.sendCreatedTrack(req))
            .isInstanceOf(KafkaServiceException.class)
            .hasMessageContaining("Error sending Track created event: boom");

    verify(kafkaTemplate, times(1)).send(eq("created_track"), any(byte[].class));
    verifyNoMoreInteractions(kafkaTemplate);
  }

  @Test
  void givenValidatorViolation_whenSendCreatedTrack_thenThrowsKafkaServiceExceptionBeforeSend() {
    // given
    var req = sampleRequest();

    @SuppressWarnings("unchecked")
    ConstraintViolation<CreatedTrackEvent> violation = mock(ConstraintViolation.class);
    when(violation.getMessage()).thenReturn("title must not be blank");
    when(validator.validate(any(CreatedTrackEvent.class))).thenReturn(Set.of(violation));

    // when + then
    assertThatThrownBy(() -> kafkaService.sendCreatedTrack(req))
            .isInstanceOf(KafkaServiceException.class)
            .hasMessage("title must not be blank");

    // Ensure no send happened
    verifyNoInteractions(kafkaTemplate);
  }
}
