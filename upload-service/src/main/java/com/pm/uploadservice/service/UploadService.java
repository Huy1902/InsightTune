package com.pm.uploadservice.service;

import com.pm.uploadservice.dto.*;
import com.pm.uploadservice.exception.KafkaServiceException;
import com.pm.uploadservice.exception.MetaExtractException;
import com.pm.uploadservice.exception.S3ServiceException;
import com.pm.uploadservice.exception.UploadServiceException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

/**
 * Represents the metadata of a song stored in object storage.
 *
 * <ul>
 *   <li><b>storageKey</b> – The unique key or path in S3
 *       where the MP3 file is stored.</li>
 *   <li><b>title</b> – The title of the song extracted from ID3 metadata.
 *       Can be {@code null} if unavailable.</li>
 *   <li><b>album</b> – The album name extracted from ID3 metadata.
 *       Can be {@code null} for singles or songs not part of an album.</li>
 *   <li><b>artists</b> – List of participating artists.
 *       Usually parsed from the raw artist string (e.g., “Artist feat. Guest”).</li>
 *   <li><b>durationMs</b> – Duration of the song in milliseconds.</li>
 *   <li><b>coverImageKey</b> – The key in S3
 *       where the cover image of the song is stored.</li>
 * </ul>
 *
 * @author Huy1902
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UploadService {
  private final MetadataService metadataService;
  private final S3Service s3Service;
  private final KafkaService kafkaService;
  private final Validator validator;

  public TrackUploadResponseDto uploadTrack(TrackUploadRequestDto trackUploadRequestDto) throws UploadServiceException {
    MetaRequestDto metaRequestDto = new MetaRequestDto(trackUploadRequestDto.getFile());
    MetaResponseDto metaResponseDto = null;
    try {
      metaResponseDto = metadataService.buildMetadata(metaRequestDto);
    } catch (MetaExtractException e) {
      log.error(e.getMessage());
      throw new UploadServiceException(e.getMessage());
    }
    String baseKey = null;
    try {
      if (Objects.equals(trackUploadRequestDto.getKey(), "")) {
        String rawKey = Objects.requireNonNull(metaResponseDto).getArtists().getFirst().trim() + metaResponseDto.getTitle().trim();

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(rawKey.getBytes());

        long value = new BigInteger(1, Arrays.copyOf(digest, 8)).longValue();
        baseKey = String.format("%012d", value); // 12 digits
      }
      else {
        baseKey = trackUploadRequestDto.getKey();
      }
      log.info("baseKey: {}", baseKey);
    } catch (NoSuchAlgorithmException e) {
      log.error(e.getMessage());
      throw new UploadServiceException(e.getMessage());
    }

    try {

      S3UploadRequestDto s3UploadRequestDto = S3UploadRequestDto.builder()
              .file(trackUploadRequestDto.getFile())
              .image(metaResponseDto.getImage())
              .imageType(metaResponseDto.getImageType())
              .key(baseKey)
              .build();
      validator.validate(s3UploadRequestDto);
      Set<ConstraintViolation<S3UploadRequestDto>> violations = validator.validate(s3UploadRequestDto);
      if (!violations.isEmpty()) {
        throw new S3ServiceException(violations.iterator().next().getMessage());
      }

      S3UploadResponseDto s3UploadResponseDto = s3Service.uploadTrack(s3UploadRequestDto);

      CreatedTrackRequestDto createdTrackRequestDto = CreatedTrackRequestDto.builder()
              .title(metaResponseDto.getTitle())
              .album(metaResponseDto.getAlbum())
              .durationMs(metaResponseDto.getDurationMs())
              .storageKey(s3UploadResponseDto.getStorageKey())
              .coverImageKey(s3UploadResponseDto.getCoverImageKey())
              .artists(metaResponseDto.getArtists()).build();

      validator.validate(createdTrackRequestDto);
      Set<ConstraintViolation<CreatedTrackRequestDto>> violations2 = validator.validate(createdTrackRequestDto);
      if (!violations2.isEmpty()) {
        throw new KafkaServiceException(violations2.iterator().next().getMessage());
      }
      CreatedTrackResponseDto createdTrackResponseDto = kafkaService.sendCreatedTrack(createdTrackRequestDto);


      TrackUploadResponseDto trackUploadResponseDto = TrackUploadResponseDto.builder()
              .title(metaResponseDto.getTitle())
              .artists(metaResponseDto.getArtists())
              .storageKey(s3UploadResponseDto.getStorageKey())
              .coverImageKey(s3UploadResponseDto.getCoverImageKey())
              .kafkaStatus(createdTrackResponseDto.getKafkaStatus())
              .s3Status(s3UploadResponseDto.getStatus())
              .build();

      Set<ConstraintViolation<TrackUploadResponseDto>> violations3 = validator.validate(trackUploadResponseDto);
      if (!violations3.isEmpty()) {
        throw new UploadServiceException(violations3.iterator().next().getMessage());
      }
      return trackUploadResponseDto;

    } catch (S3ServiceException | KafkaServiceException | UploadServiceException e) {
      log.error(e.getMessage());
      throw new UploadServiceException(e.getMessage());
    }
  }
}
