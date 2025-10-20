package com.pm.catalogservice.service;

import com.pm.catalogservice.dto.request.NextSongRequestDto;
import com.pm.catalogservice.dto.response.TrackResponseDto;
import com.pm.catalogservice.mapper.TrackMapper;
import com.pm.catalogservice.model.Artist;
import com.pm.catalogservice.model.Track;
import com.pm.catalogservice.repository.ArtistRepository;
import com.pm.catalogservice.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class TrackService {
  private final TrackRepository trackRepository;
  private final ArtistRepository artistRepository;

  public List<TrackResponseDto> getTracks() {
    List<Track> tracks = trackRepository.findAll();

    return tracks.stream()
            .map(TrackMapper::toTrackResponseDto).toList();
  }

  public List<TrackResponseDto> getTracksByKeyword(String keyword) {
    List<Track> matchedTracks = trackRepository.findByTitleContainingIgnoreCase(keyword);
    List<Artist> matchedArtist = artistRepository.findByNameContainingIgnoreCase(keyword);

    if (matchedTracks.isEmpty() && matchedArtist.isEmpty()) {
      return Collections.emptyList();
    }

    List<Track> relatedTracks = trackRepository.findByArtistsIn(matchedArtist);
    // gộp và loại trùng
    return Stream.concat(matchedTracks.stream(), relatedTracks.stream())
            .distinct()
            .map(TrackMapper::toTrackResponseDto)
            .toList();
  }

  public List<TrackResponseDto> getNextSong(NextSongRequestDto nextSongRequestDto) {
    UUID albumId = nextSongRequestDto.albumId();
    UUID currentTrackId = nextSongRequestDto.currentTrackId();

    List<Track> nextTracks = new ArrayList<>();
    List<Track> albumTracks = trackRepository.findAllByAlbumOrdered(albumId);

    if (albumTracks != null && !albumTracks.isEmpty())
    {
      int currentIndex = IntStream.range(0, albumTracks.size())
              .filter(i -> albumTracks.get(i).getId().equals(currentTrackId))
              .findFirst()
              .orElse(-1);

      for (int i = 1; i <= 5; i++) {
        int nextIndex = (currentIndex + i) % albumTracks.size();
        if (nextIndex == currentIndex) break;
        nextTracks.add(albumTracks.get(nextIndex));
      }
    }

    if (nextTracks.size() < 5) {
      List<Track> artistTracks = trackRepository.findAllByArtistsOrdered(nextSongRequestDto.artists());

      // tìm vị trí bài hiện tại trong danh sách nghệ sĩ
      int currentArtistIndex = IntStream.range(0, artistTracks.size())
              .filter(i -> artistTracks.get(i).getId().equals(currentTrackId))
              .findFirst()
              .orElse(-1);

      for (int i = 1; i <= 5 - nextTracks.size() && currentArtistIndex >= 0; i++) {
        int nextIndex = (currentArtistIndex + i) % artistTracks.size();
        Track nextTrack = artistTracks.get(nextIndex);
        if (!nextTracks.contains(nextTrack)) {
          nextTracks.add(nextTrack);
        }
      }

      if (nextTracks.size() <= 5) {
        nextTracks.addAll(trackRepository.findRandomTracks(PageRequest.of(0, 5 - nextTracks.size())));
      }
    }

    return nextTracks.stream()
            .map(TrackMapper::toTrackResponseDto)
            .distinct()
            .toList();
  }

  public List<TrackResponseDto> getAllTrackByIds(Set<UUID> ids) {
    List<Track> tracks =  trackRepository.findTracksByIdIn(ids);

    return tracks.stream()
            .distinct()
            .map(TrackMapper::toTrackResponseDto)
            .toList();
  }
}
