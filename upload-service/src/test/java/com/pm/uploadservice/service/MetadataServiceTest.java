package com.pm.uploadservice.service;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import com.pm.uploadservice.dto.MetaRequestDto;
import com.pm.uploadservice.dto.MetaResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetadataServiceTest {

  private final MetadataService service = new MetadataService();

  @Test
  void givenId3v2Tags_whenBuildMetadata_thenParsesFieldsSplitsArtistsAndNormalizesMime() throws Exception {
    // given
    // Simulate an uploaded file (content does not matter; Mp3File is mocked)
    var file = new MockMultipartFile("file", "love-story.mp3", "audio/mpeg", new byte[]{1,2,3});
    var req = new MetaRequestDto(file);

    try (MockedConstruction<Mp3File> mocked = mockConstruction(Mp3File.class, (mp3, ctx) -> {
      when(mp3.getLengthInSeconds()).thenReturn(123L); // -> 123_000 ms
      when(mp3.hasId3v2Tag()).thenReturn(true);

      ID3v2 tag = mock(ID3v2.class);
      when(mp3.getId3v2Tag()).thenReturn(tag);

      // Provide noisy values to exercise trimming + splitting + mime normalization
      when(tag.getTitle()).thenReturn("  Love Story  ");
      when(tag.getAlbum()).thenReturn("  Fearless  ");
      when(tag.getArtist()).thenReturn(" Taylor Swift feat.  Someone & DJ X, Guest ");
      when(tag.getAlbumImage()).thenReturn(new byte[]{9,9,9});
      when(tag.getAlbumImageMimeType()).thenReturn("image/jpg"); // should normalize to image/jpeg
    })) {
      // when
      MetaResponseDto out = service.buildMetadata(req);

      // then
      assertThat(out.getDurationMs()).isEqualTo(123_000);
      assertThat(out.getTitle()).isEqualTo("Love Story");
      assertThat(out.getAlbum()).isEqualTo("Fearless");

      // Artist splitting on feat./&, comma, x
      assertThat(out.getArtists())
              .containsExactly("Taylor Swift", "Someone", "DJ X", "Guest");

      assertThat(out.getImage()).isEqualTo(new byte[]{9,9,9});
      assertThat(out.getImageType()).isEqualTo("image/jpeg"); // normalized

      // Verify Mp3File constructed once with the temp file path
      assertThat(mocked.constructed()).hasSize(1);
      verify(mocked.constructed().get(0), times(1)).getLengthInSeconds();
    }
  }

  @Test
  void givenNoId3v2_whenBuildMetadata_thenFallsBackToFilenameAndNoArtistsOrImage() throws Exception {
    var file = new MockMultipartFile("file", "My Song.NAME.mp3", "audio/mpeg", new byte[]{});
    var req = new MetaRequestDto(file);

    try (MockedConstruction<Mp3File> mocked = mockConstruction(Mp3File.class, (mp3, ctx) -> {
      when(mp3.getLengthInSeconds()).thenReturn(1L);
      when(mp3.hasId3v2Tag()).thenReturn(false);
    })) {
      MetaResponseDto out = service.buildMetadata(req);

      assertThat(out.getTitle()).isEqualTo("My Song.NAME"); // filename fallback (no extension)
      assertThat(out.getAlbum()).isEqualTo("");
      assertThat(out.getArtists()).isEmpty();
      assertThat(out.getImage()).isNull();
      assertThat(out.getImageType()).isNull();
      assertThat(out.getDurationMs()).isEqualTo(1000);
    }
  }

  @Test
  void givenUnsupportedImageMime_whenBuildMetadata_thenKeepsMimeButNoException() throws Exception {
    var file = new MockMultipartFile("file", "weird.mp3", "audio/mpeg", new byte[]{});
    var req = new MetaRequestDto(file);

    try (MockedConstruction<Mp3File> mocked = mockConstruction(Mp3File.class, (mp3, ctx) -> {
      when(mp3.getLengthInSeconds()).thenReturn(42L);
      when(mp3.hasId3v2Tag()).thenReturn(true);

      ID3v2 tag = mock(ID3v2.class);
      when(mp3.getId3v2Tag()).thenReturn(tag);

      // Title missing → fallback to filename
      when(tag.getTitle()).thenReturn("  ");
      when(tag.getAlbum()).thenReturn(null);
      when(tag.getArtist()).thenReturn(null);
      when(tag.getAlbumImage()).thenReturn(new byte[]{7});
      when(tag.getAlbumImageMimeType()).thenReturn(" image/gif "); // unsupported; service logs error only
    })) {
      MetaResponseDto out = service.buildMetadata(req);

      assertThat(out.getTitle()).isEqualTo("weird");
      assertThat(out.getAlbum()).isEqualTo("");
      assertThat(out.getArtists()).isEmpty();
      assertThat(out.getImage()).isEqualTo(new byte[]{7});
      assertThat(out.getImageType()).isEqualTo("image/gif"); // preserved, not thrown
      assertThat(out.getDurationMs()).isEqualTo(42_000);
    }
  }

  @Test
  void givenArtistDelimiters_whenSplit_thenAllNamesCleaned() throws Exception {
    var file = new MockMultipartFile("file", "x.mp3", "audio/mpeg", new byte[]{});
    var req = new MetaRequestDto(file);

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
}
