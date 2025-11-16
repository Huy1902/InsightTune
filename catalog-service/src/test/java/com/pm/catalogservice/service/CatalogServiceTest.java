package com.pm.catalogservice.service;

import com.pm.catalogservice.model.Album;
import com.pm.catalogservice.model.Artist;
import com.pm.catalogservice.model.Track;
import com.pm.catalogservice.repository.AlbumRepository;
import com.pm.catalogservice.repository.ArtistRepository;
import com.pm.catalogservice.repository.TrackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import track.events.CreatedTrackEvent;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {

  @Mock private ArtistRepository artistRepository;
  @Mock private AlbumRepository albumRepository;
  @Mock private TrackRepository trackRepository;

  @InjectMocks
  private CatalogService catalogService;

  private static final String STORAGE_KEY = "tracks/12345.mp3";

  private CreatedTrackEvent buildEvent(String title,
                                       String album,
                                       List<String> artists,
                                       int durationMs,
                                       String coverImageKey) {
    return CreatedTrackEvent.newBuilder()
            .setStorageKey(STORAGE_KEY)
            .setTitle(title)
            .setAlbum(album == null ? "" : album)
            .addAllArtists(artists == null ? List.of() : artists)
            .setDurationMs(durationMs)
            .setCoverImageKey(coverImageKey)
            .build();
  }

  @BeforeEach
  void setUp() {}

  @Test
  void givenTrackDoesNotExist_whenIngestTrack_thenCreatesNewTrackWithAlbumAndArtists() {
    // given
    CreatedTrackEvent event = buildEvent(
            "Hello",
            "25",
            List.of("Adele", "Greg Kurstin"),
            200000,
            "covers/hello.jpg"
    );

    when(trackRepository.findTrackByStorageKey(STORAGE_KEY))
            .thenReturn(Optional.empty());

    // Album not found -> create
    when(albumRepository.findByTitleIgnoreCase("25")).thenReturn(Optional.empty());
    Album savedAlbum = Album.builder().id(UUID.randomUUID()).title("25").build();
    when(albumRepository.save(any(Album.class))).thenReturn(savedAlbum);

    // Artists not found -> create (null-safe argThat)
    when(artistRepository.findByNameIgnoreCase("Adele")).thenReturn(Optional.empty());
    when(artistRepository.findByNameIgnoreCase("Greg Kurstin")).thenReturn(Optional.empty());
    Artist adele = Artist.builder().id(UUID.randomUUID()).name("Adele").build();
    Artist greg = Artist.builder().id(UUID.randomUUID()).name("Greg Kurstin").build();
    when(artistRepository.save(argThat(a -> a != null && "Adele".equals(a.getName())))).thenReturn(adele);
    when(artistRepository.save(argThat(a -> a != null && "Greg Kurstin".equals(a.getName())))).thenReturn(greg);

    ArgumentCaptor<Track> trackCaptor = ArgumentCaptor.forClass(Track.class);

    // when
    catalogService.ingestTrack(event);

    // then
    verify(trackRepository).save(trackCaptor.capture());
    Track saved = trackCaptor.getValue();
    assertThat(saved.getTitle()).isEqualTo("Hello");
    assertThat(saved.getStorageKey()).isEqualTo(STORAGE_KEY);
    assertThat(saved.getDurationMs()).isEqualTo(200000);
    assertThat(saved.getCoverImageKey()).isEqualTo("covers/hello.jpg");
    assertThat(saved.getAlbum()).isSameAs(savedAlbum);
    assertThat(saved.getArtists()).containsExactlyInAnyOrder(adele, greg);

    verify(albumRepository).findByTitleIgnoreCase("25");
    verify(artistRepository).findByNameIgnoreCase("Adele");
    verify(artistRepository).findByNameIgnoreCase("Greg Kurstin");
  }

  @Test
  void givenTrackAlreadyExists_whenIngestTrack_thenUpdatesTrackWithNewAlbumAndArtists() {
    // given existing track
    Track existing = Track.builder()
            .id(UUID.randomUUID())
            .title("Old Title")
            .storageKey(STORAGE_KEY)
            .durationMs(1000)
            .coverImageKey(null)
            .album(null)
            .artists(new LinkedHashSet<>())
            .build();

    when(trackRepository.findTrackByStorageKey(STORAGE_KEY))
            .thenReturn(Optional.of(existing));

    // incoming event
    CreatedTrackEvent event = buildEvent(
            "New Title",
            "30",
            List.of("New Artist"),
            180000,
            "covers/new.jpg"
    );

    // album found (no create)
    Album existingAlbum = Album.builder().id(UUID.randomUUID()).title("30").build();
    when(albumRepository.findByTitleIgnoreCase("30")).thenReturn(Optional.of(existingAlbum));

    // artist found (no create)
    Artist existingArtist = Artist.builder().id(UUID.randomUUID()).name("New Artist").build();
    when(artistRepository.findByNameIgnoreCase("New Artist")).thenReturn(Optional.of(existingArtist));

    // when
    catalogService.ingestTrack(event);

    // then
    assertThat(existing.getTitle()).isEqualTo("New Title");
    assertThat(existing.getDurationMs()).isEqualTo(180000);
    assertThat(existing.getCoverImageKey()).isEqualTo("covers/new.jpg");
    assertThat(existing.getAlbum()).isSameAs(existingAlbum);
    assertThat(existing.getArtists()).containsExactly(existingArtist);

    verify(albumRepository).findByTitleIgnoreCase("30");
    verify(artistRepository).findByNameIgnoreCase("New Artist");
    verify(trackRepository, never()).save(any(Track.class));
  }

  @Test
  void givenTrackWithAlbum_whenIngestTrackWithBlankAlbum_thenClearsAlbum() {
    // given existing track with album
    Album oldAlbum = Album.builder().id(UUID.randomUUID()).title("Old").build();
    Track existing = Track.builder()
            .id(UUID.randomUUID())
            .title("Has Album")
            .storageKey(STORAGE_KEY)
            .album(oldAlbum)
            .artists(new LinkedHashSet<>())
            .build();

    when(trackRepository.findTrackByStorageKey(STORAGE_KEY))
            .thenReturn(Optional.of(existing));

    // event: blank album -> should clear
    CreatedTrackEvent event = buildEvent(
            "Keep Title",
            "",
            List.of("ArtistX"),
            1234,
            "covers/x.jpg"
    );

    // artist found
    Artist artistX = Artist.builder().id(UUID.randomUUID()).name("ArtistX").build();
    when(artistRepository.findByNameIgnoreCase("ArtistX")).thenReturn(Optional.of(artistX));

    // when
    catalogService.ingestTrack(event);

    // then
    assertThat(existing.getAlbum()).isNull();
    assertThat(existing.getArtists()).containsExactly(artistX);
    assertThat(existing.getTitle()).isEqualTo("Keep Title");
    assertThat(existing.getDurationMs()).isEqualTo(1234);
    assertThat(existing.getCoverImageKey()).isEqualTo("covers/x.jpg");

    verify(albumRepository, never()).findByTitleIgnoreCase(anyString());
    verify(trackRepository, never()).save(any());
  }

  @Test
  void givenNoAlbumInEvent_whenIngestTrack_thenCreatesTrackWithoutAlbumAndWithArtists() {
    // given no existing track
    when(trackRepository.findTrackByStorageKey(STORAGE_KEY)).thenReturn(Optional.empty());

    // event with blank album
    CreatedTrackEvent event = buildEvent(
            "Solo Single",
            "   ",
            List.of("Soloist"),
            150000,
            "covers/solo.jpg"
    );

    // artist not found -> create
    when(artistRepository.findByNameIgnoreCase("Soloist")).thenReturn(Optional.empty());
    Artist solo = Artist.builder().id(UUID.randomUUID()).name("Soloist").build();
    when(artistRepository.save(any(Artist.class))).thenReturn(solo);

    ArgumentCaptor<Track> trackCaptor = ArgumentCaptor.forClass(Track.class);

    // when
    catalogService.ingestTrack(event);

    // then
    verify(trackRepository).save(trackCaptor.capture());
    Track saved = trackCaptor.getValue();
    assertThat(saved.getAlbum()).isNull();
    assertThat(saved.getArtists()).containsExactly(solo);
    assertThat(saved.getTitle()).isEqualTo("Solo Single");

    verify(albumRepository, never()).findByTitleIgnoreCase(anyString());
    verify(albumRepository, never()).save(any());
  }
}
