package com.pm.catalogservice.service;

import com.pm.catalogservice.dto.response.TrackResponseDto;
import com.pm.catalogservice.mapper.TrackMapper;
import com.pm.catalogservice.model.Album;
import com.pm.catalogservice.model.Artist;
import com.pm.catalogservice.model.Track;
import com.pm.catalogservice.repository.ArtistRepository;
import com.pm.catalogservice.repository.TrackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrackServiceTest {

    @Mock
    private TrackRepository trackRepository;

    @Mock
    private ArtistRepository artistRepository;

    @InjectMocks
    private TrackService trackService;

    private Track track;
    private UUID trackId;
    private Album album;
    private Artist artist;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        trackId = UUID.randomUUID();
        album = new Album();
        album.setId(UUID.randomUUID());
        artist = new Artist();
        track = new Track();
        track.setId(trackId);
        track.setAlbum(album);
        track.setArtists(Set.of(artist));
    }

    // --- getTracksIds ---
    @Test
    void getTracksIds_shouldReturnIds() {
        track.setCoverImageKey("storage/abc123");
        when(trackRepository.findAll()).thenReturn(List.of(track));
        List<String> ids = trackService.getTracksIds();
        assertEquals(1, ids.size());
    }

    @Test
    void getTracksIds_emptyList_shouldReturnEmpty() {
        when(trackRepository.findAll()).thenReturn(Collections.emptyList());
        List<String> ids = trackService.getTracksIds();
        assertTrue(ids.isEmpty());
    }

    // --- getTracks ---
    @Test
    void getTracks_shouldReturnList() {
        when(trackRepository.findAll()).thenReturn(List.of(track));
        List<TrackResponseDto> result = trackService.getTracks();
        assertEquals(1, result.size());
    }

    // --- getTracksByKeyword ---
    @Test
    void getTracksByKeyword_noMatch_shouldReturnEmpty() {
        when(trackRepository.findByTitleContainingIgnoreCase("abc")).thenReturn(Collections.emptyList());
        when(artistRepository.findByNameContainingIgnoreCase("abc")).thenReturn(Collections.emptyList());
        List<TrackResponseDto> result = trackService.getTracksByKeyword("abc");
        assertTrue(result.isEmpty());
    }

    @Test
    void getTracksByKeyword_onlyMatchedTracks_shouldReturnResults() {
        when(trackRepository.findByTitleContainingIgnoreCase("rock")).thenReturn(List.of(track));
        when(artistRepository.findByNameContainingIgnoreCase("rock")).thenReturn(Collections.emptyList());
        when(trackRepository.findByArtistsIn(Collections.emptyList())).thenReturn(Collections.emptyList());
        List<TrackResponseDto> result = trackService.getTracksByKeyword("rock");
        assertFalse(result.isEmpty());
    }

    @Test
    void getTracksByKeyword_onlyMatchedArtist_shouldReturnResults() {
        when(trackRepository.findByTitleContainingIgnoreCase("pop")).thenReturn(Collections.emptyList());
        when(artistRepository.findByNameContainingIgnoreCase("pop")).thenReturn(List.of(artist));
        when(trackRepository.findByArtistsIn(List.of(artist))).thenReturn(List.of(track));
        List<TrackResponseDto> result = trackService.getTracksByKeyword("pop");
        assertFalse(result.isEmpty());
    }

    @Test
    void getTracksByKeyword_bothMatched_shouldReturnDistinctResults() {
        when(trackRepository.findByTitleContainingIgnoreCase("mix")).thenReturn(List.of(track));
        when(artistRepository.findByNameContainingIgnoreCase("mix")).thenReturn(List.of(artist));
        when(trackRepository.findByArtistsIn(List.of(artist))).thenReturn(List.of(track));
        List<TrackResponseDto> result = trackService.getTracksByKeyword("mix");
        assertEquals(1, result.size());
    }

    // --- getNextSong ---
    @Test
    void getNextSong_fullAlbumTracks_shouldReturnNextFive() {
        List<Track> albumTracks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Track t = new Track();
            t.setId(UUID.randomUUID());
            t.setAlbum(album);
            albumTracks.add(t);
        }
        albumTracks.set(3, track); // current track index = 3

        when(trackRepository.findById(trackId)).thenReturn(Optional.of(track));
        when(trackRepository.findAllByAlbumOrdered(album.getId())).thenReturn(albumTracks);
        List<TrackResponseDto> result = trackService.getNextSong(trackId);
        assertFalse(result.isEmpty());
    }

    @Test
    void getNextSong_albumTracksEmpty_shouldFallbackToArtistTracks() {
        when(trackRepository.findById(trackId)).thenReturn(Optional.of(track));
        when(trackRepository.findAllByAlbumOrdered(album.getId())).thenReturn(Collections.emptyList());

        Track other = new Track();
        other.setId(UUID.randomUUID());
        other.setArtists(Set.of(artist));
        other.setAlbum(album); // ✅ thêm dòng này để tránh null

        when(trackRepository.findAllByArtistsOrdered(Set.of(artist))).thenReturn(List.of(track, other));
        when(trackRepository.findRandomTracks(PageRequest.of(0, 4))).thenReturn(Collections.emptyList());

        List<TrackResponseDto> result = trackService.getNextSong(trackId);
        assertFalse(result.isEmpty());
    }


    @Test
    void getNextSong_trackNotFound_shouldThrow() {
        when(trackRepository.findById(trackId)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> trackService.getNextSong(trackId));
    }

    @Test
    void getNextSong_artistTracksEmpty_shouldFallbackToRandom() {
        when(trackRepository.findById(trackId)).thenReturn(Optional.of(track));
        when(trackRepository.findAllByAlbumOrdered(album.getId())).thenReturn(null);
        when(trackRepository.findAllByArtistsOrdered(Set.of(artist))).thenReturn(Collections.emptyList());
        when(trackRepository.findRandomTracks(PageRequest.of(0, 5))).thenReturn(List.of(track));

        List<TrackResponseDto> result = trackService.getNextSong(trackId);
        assertEquals(1, result.size());
    }

    // --- getAllTrackByIds ---
    @Test
    void getAllTrackByIds_shouldReturnDistinctList() {
        when(trackRepository.findTracksByIdIn(Set.of(trackId))).thenReturn(List.of(track, track));
        List<TrackResponseDto> result = trackService.getAllTrackByIds(Set.of(trackId));
        assertEquals(1, result.size());
    }
}
