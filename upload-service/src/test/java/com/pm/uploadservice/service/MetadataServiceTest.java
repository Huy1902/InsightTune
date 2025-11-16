package com.pm.uploadservice.service;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import com.pm.uploadservice.dto.MetaRequestDto;
import com.pm.uploadservice.dto.MetaResponseDto;
import com.pm.uploadservice.exception.MetaExtractException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetadataServiceTest {

  @Mock
  Validator validator;

  @InjectMocks
  MetadataService service;

  @Test
  void givenId3v2Tags_whenBuildMetadata_thenParsesFieldsSplitsArtistsAndNormalizesMime() throws Exception {
    // given: non-empty file (empty would trigger "No file provided")
    var file = new MockMultipartFile("file", "love-story.mp3", "audio/mpeg", new byte[]{1, 2, 3});
    var req = new MetaRequestDto(file);

    // validation passes
    when(validator.validate(any())).thenReturn(Collections.emptySet());

    try (MockedConstruction<Mp3File> mocked = mockConstruction(Mp3File.class, (mp3, ctx) -> {
      when(mp3.getLengthInSeconds()).thenReturn(123L); // -> 123_000 ms
      when(mp3.hasId3v2Tag()).thenReturn(true);

      ID3v2 tag = mock(ID3v2.class);
      when(mp3.getId3v2Tag()).thenReturn(tag);

      // exercise trimming/splitting/mime normalization
      when(tag.getTitle()).thenReturn("  Love Story  ");
      when(tag.getAlbum()).thenReturn("  Fearless  ");
      when(tag.getArtist()).thenReturn(" Taylor Swift feat.  Someone & DJ X, Guest ");
      when(tag.getAlbumImage()).thenReturn(new byte[]{9, 9, 9});
      when(tag.getAlbumImageMimeType()).thenReturn("image/jpg"); // -> image/jpeg
    })) {
      // when
      MetaResponseDto out = service.buildMetadata(req);

      // then
      assertThat(out.getDurationMs()).isEqualTo(123_000);
      assertThat(out.getTitle()).isEqualTo("Love Story");
      assertThat(out.getAlbum()).isEqualTo("Fearless");
      assertThat(out.getArtists()).containsExactly("Taylor Swift", "Someone", "DJ X", "Guest");
      assertThat(out.getImage()).isEqualTo(new byte[]{9, 9, 9});
      assertThat(out.getImageType()).isEqualTo("image/jpeg");

      assertThat(mocked.constructed()).hasSize(1);
      verify(mocked.constructed().get(0), times(1)).getLengthInSeconds();
    }
  }

  @Test
  void givenNoId3v2_whenBuildMetadata_thenFallsBackToFilenameAndNoArtistsOrImage() throws Exception {
    // non-empty to avoid "No file provided"
    var file = new MockMultipartFile("file", "My Song.NAME.mp3", "audio/mpeg", new byte[]{1});
    var req = new MetaRequestDto(file);

    when(validator.validate(any())).thenReturn(Collections.emptySet());

    try (MockedConstruction<Mp3File> mocked = mockConstruction(Mp3File.class, (mp3, ctx) -> {
      when(mp3.getLengthInSeconds()).thenReturn(1L);
      when(mp3.hasId3v2Tag()).thenReturn(false);
    })) {
      MetaResponseDto out = service.buildMetadata(req);

      assertThat(out.getTitle()).isEqualTo("My Song.NAME"); // filename fallback
      assertThat(out.getAlbum()).isEqualTo("");
      assertThat(out.getArtists()).isEmpty();
      assertThat(out.getImage()).isNull();
      assertThat(out.getImageType()).isNull();
      assertThat(out.getDurationMs()).isEqualTo(1_000);
    }
  }

  @Test
  void givenUnsupportedImageMime_whenBuildMetadata_thenKeepsMimeButNoException() throws Exception {
    // non-empty
    var file = new MockMultipartFile("file", "weird.mp3", "audio/mpeg", new byte[]{1});
    var req = new MetaRequestDto(file);

    when(validator.validate(any())).thenReturn(Collections.emptySet());

    try (MockedConstruction<Mp3File> mocked = mockConstruction(Mp3File.class, (mp3, ctx) -> {
      when(mp3.getLengthInSeconds()).thenReturn(42L);
      when(mp3.hasId3v2Tag()).thenReturn(true);

      ID3v2 tag = mock(ID3v2.class);
      when(mp3.getId3v2Tag()).thenReturn(tag);

      // Title missing -> fallback to filename
      when(tag.getTitle()).thenReturn("  ");
      when(tag.getAlbum()).thenReturn(null);
      when(tag.getArtist()).thenReturn(null);
      when(tag.getAlbumImage()).thenReturn(new byte[]{7});
      when(tag.getAlbumImageMimeType()).thenReturn(" image/gif "); // unsupported; service only logs
    })) {
      MetaResponseDto out = service.buildMetadata(req);

      assertThat(out.getTitle()).isEqualTo("weird");
      assertThat(out.getAlbum()).isEqualTo("");
      assertThat(out.getArtists()).isEmpty();
      assertThat(out.getImage()).isEqualTo(new byte[]{7});
      assertThat(out.getImageType()).isEqualTo("image/gif"); // preserved
      assertThat(out.getDurationMs()).isEqualTo(42_000);
    }
  }

  @Test
  void givenArtistDelimiters_whenSplit_thenAllNamesCleaned() throws Exception {
    // non-empty
    var file = new MockMultipartFile("file", "x.mp3", "audio/mpeg", new byte[]{1});
    var req = new MetaRequestDto(file);

    when(validator.validate(any())).thenReturn(Collections.emptySet());

    try (MockedConstruction<Mp3File> mocked = mockConstruction(Mp3File.class, (mp3, ctx) -> {
      when(mp3.getLengthInSeconds()).thenReturn(10L);
      when(mp3.hasId3v2Tag()).thenReturn(true);

      ID3v2 tag = mock(ID3v2.class);
      when(mp3.getId3v2Tag()).thenReturn(tag);

      when(tag.getTitle()).thenReturn("Song");
      when(tag.getAlbum()).thenReturn("Album");
      when(tag.getArtist()).thenReturn("A & B, C x D ft. E feat. F");
      when(tag.getAlbumImage()).thenReturn(null);
      when(tag.getAlbumImageMimeType()).thenReturn(null);
    })) {
      MetaResponseDto out = service.buildMetadata(req);
      assertThat(out.getArtists()).containsExactly("A", "B", "C", "D", "E", "F");
    }
  }

  @Test
  void givenValidationViolation_whenBuildMetadata_thenThrowsMetaExtractException() throws Exception {
    // non-empty file so we don’t hit "No file provided"
    var file = new MockMultipartFile("file", "bad.mp3", "audio/mpeg", new byte[]{1});
    var req = new MetaRequestDto(file);

    // stub validator to return a violation
    @SuppressWarnings("unchecked")
    ConstraintViolation<MetaResponseDto> violation = mock(ConstraintViolation.class);
    when(violation.getMessage()).thenReturn("Title must not be blank");
    when(validator.validate(any(MetaResponseDto.class))).thenReturn(Set.of(violation));

    try (MockedConstruction<Mp3File> mocked = mockConstruction(Mp3File.class, (mp3, ctx) -> {
      when(mp3.getLengthInSeconds()).thenReturn(10L);
      when(mp3.hasId3v2Tag()).thenReturn(false); // fallback to filename
    })) {
      assertThatThrownBy(() -> service.buildMetadata(req))
              .isInstanceOf(MetaExtractException.class)
              .hasMessage("Title must not be blank");
    }
  }
}
