package com.pm.uploadservice.service;

import com.pm.uploadservice.dto.CreatedTrackRequestDto;
import com.pm.uploadservice.dto.CreatedTrackResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import track.events.CreatedTrackEvent;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaServiceTest {

  @Mock
  KafkaTemplate<String, byte[]> kafkaTemplate;

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
    // We don't need to stub return; service doesn't use the future
    // when(kafkaTemplate.send(...)).thenReturn(null); // default is fine

    // when
    CreatedTrackResponseDto resp = kafkaService.sendCreatedTrack(req);

    // then
    assertThat(resp.getKafkaStatus()).isEqualTo("Successfully sent event");

    // capture the bytes sent to Kafka and parse to verify fields
    ArgumentCaptor<byte[]> bytesCaptor = ArgumentCaptor.forClass(byte[].class);
    verify(kafkaTemplate, times(1)).send(eq("created_track"), bytesCaptor.capture());

    byte[] sent = bytesCaptor.getValue();
    CreatedTrackEvent event = CreatedTrackEvent.parseFrom(sent);

    assertThat(event.getTitle()).isEqualTo("Love Story");
    assertThat(event.getAlbum()).isEqualTo("Fearless");
    assertThat(event.getArtistsList()).containsExactly("Taylor Swift", "Guest");
    assertThat(event.getDurationMs()).isEqualTo(230_000);
    assertThat(event.getCoverImageKey()).isEqualTo("covers/abc123.jpg");
    assertThat(event.getStorageKey()).isEqualTo("tracks/abc123.mp3");

    verifyNoMoreInteractions(kafkaTemplate);
  }

  @Test
  void givenKafkaTemplateThrows_whenSendCreatedTrack_thenReturnsFailure() {
    // given
    var req = sampleRequest();
    when(kafkaTemplate.send(eq("created_track"), any(byte[].class)))
            .thenThrow(new RuntimeException("boom"));

    // when
    CreatedTrackResponseDto resp = kafkaService.sendCreatedTrack(req);

    // then
    assertThat(resp.getKafkaStatus()).isEqualTo("Failed to sent event");
    verify(kafkaTemplate, times(1)).send(eq("created_track"), any(byte[].class));
    verifyNoMoreInteractions(kafkaTemplate);
  }
}
