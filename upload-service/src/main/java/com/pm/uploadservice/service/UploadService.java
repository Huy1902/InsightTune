package com.pm.uploadservice.service;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.UnsupportedTagException;
import com.pm.uploadservice.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents metadata for a music track stored in object storage.
 *
 * <ul>
 *   <li><b>storageKey</b> – The unique key or path in S3
 *       where the MP3 file is stored.</li>
 *   <li><b>title</b> – The track title parsed from ID3 metadata.
 *       May be {@code null} if unavailable.</li>
 *   <li><b>album</b> – The album name parsed from ID3 metadata.
 *       May be {@code null} for singles or non-album tracks.</li>
 *   <li><b>artists</b> – A list of contributing artists.
 *       Typically split from a raw artist string (e.g., “Artist feat. Guest”).</li>
 *   <li><b>durationMs</b> – The track duration in milliseconds.</li>
 *   <li><b>coverImageKey</b> – The key in S3
 *       where the track’s cover image is stored.</li>
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

  public TrackUploadResponseDto uploadTrack(TrackUploadRequestDto trackUploadRequestDto) {
    MetaRequestDto metaRequestDto = new MetaRequestDto(trackUploadRequestDto.getFile());
    MetaResponseDto metaResponseDto = null;
    try {
      metaResponseDto = metadataService.buildMetadata(metaRequestDto);
    } catch (IOException | InvalidDataException | UnsupportedTagException e) {
      log.error(e.getMessage());
    }
    String baseKey = Objects.requireNonNull(metaResponseDto).getArtists().toString() + "/" + metaResponseDto.getTitle();
    S3UploadRequestDto s3UploadRequestDto = S3UploadRequestDto.builder()
            .file(trackUploadRequestDto.getFile())
            .image(metaResponseDto.getImage())
            .imageType(metaResponseDto.getImageType())
            .key(baseKey)
            .build();
    S3UploadResponseDto s3UploadResponseDto = s3Service.uploadTrack(s3UploadRequestDto);

    CreatedTrackResponseDto createdTrackResponseDto = kafkaService.sendCreatedTrack(CreatedTrackRequestDto.builder()
            .title(metaResponseDto.getTitle())
            .album(metaResponseDto.getAlbum())
            .durationMs(metaResponseDto.getDurationMs())
            .storageKey(s3UploadResponseDto.getStorageKey())
            .coverImageKey(s3UploadResponseDto.getCoverImageKey())
            .artists(metaResponseDto.getArtists()).build());

    return TrackUploadResponseDto.builder()
            .title(metaResponseDto.getTitle())
            .artists(metaResponseDto.getArtists())
            .storageKey(s3UploadResponseDto.getStorageKey())
            .coverImageKey(s3UploadResponseDto.getCoverImageKey())
            .kafkaStatus(createdTrackResponseDto.getKafkaStatus())
            .s3Status(s3UploadResponseDto.getStatus())
            .build();
  }
}
