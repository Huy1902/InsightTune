package com.pm.uploadservice.service;

import com.pm.uploadservice.dto.CreatedTrackRequestDto;
import com.pm.uploadservice.dto.CreatedTrackResponseDto;
import com.pm.uploadservice.exception.KafkaServiceException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import track.events.CreatedTrackEvent;

import java.util.Set;

/**
 * Service class receives created track from {@link UploadService}, make a {@link CreatedTrackEvent}
 * then sending an event to kafka server with topic <code>created_track</code>
 *
 * @author Huy1902
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaService {
  private final KafkaTemplate<String, byte[]> kafkaTemplate;
  private final Validator validator;

  /**
   * Transfer created Track into event for sending to server
   *
   * @param createdTrackRequestDto the created Track notify needed to be sent
   * @return a {@link CreatedTrackResponseDto} contain kafka event sending status
   */
  public CreatedTrackResponseDto sendCreatedTrack(CreatedTrackRequestDto createdTrackRequestDto)
          throws KafkaServiceException {
    CreatedTrackEvent createdTrackEvent = CreatedTrackEvent.newBuilder()
            .setTitle(createdTrackRequestDto.getTitle())
            .setAlbum(createdTrackRequestDto.getAlbum())
            .addAllArtists(createdTrackRequestDto.getArtists())
            .setDurationMs(createdTrackRequestDto.getDurationMs())
            .setCoverImageKey(createdTrackRequestDto.getCoverImageKey())
            .setStorageKey(createdTrackRequestDto.getStorageKey())
            .build();
    Set<ConstraintViolation<CreatedTrackEvent>> violations = validator.validate(createdTrackEvent);

    if (!violations.isEmpty()) {
      log.error(violations.iterator().next().getMessage());
      throw new KafkaServiceException(violations.iterator().next().getMessage());
    }


    try {
      log.info("Send Created Track Event: {}", createdTrackEvent);
      kafkaTemplate.send("created_track", createdTrackEvent.toByteArray());
    } catch (Exception e) {
      log.error("Error sending Track created event: {}", e.getMessage());
      throw new KafkaServiceException("Error sending Track created event: " + e.getMessage());
    }
    return new CreatedTrackResponseDto("Successfully sent event");
  }
}
