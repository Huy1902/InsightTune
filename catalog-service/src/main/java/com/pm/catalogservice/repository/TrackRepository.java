package com.pm.catalogservice.repository;

import com.pm.catalogservice.model.Artist;
import com.pm.catalogservice.model.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface TrackRepository extends JpaRepository<Track, UUID> {
  // Tìm theo tiêu đề (title)
  List<Track> findByTitleContainingIgnoreCase(String title);

  // Tìm bài có ít nhất 1 artist thuộc danh sách truyền vào
  @Query("SELECT DISTINCT t FROM Track t JOIN t.artists a WHERE a IN :artists")
  List<Track> findByArtistsIn(@Param("artists") List<Artist> artists);

  Optional<Track> findTrackByStorageKey(@RequestParam String storageKey);


  @Query("""
    SELECT t FROM Track t
    WHERE t.album.id = :albumId
    ORDER BY t.id
""")
  List<Track> findAllByAlbumOrdered(@Param("albumId") UUID albumId);

  // Lấy tất cả bài có cùng nghệ sĩ (trong set artist)
  @Query("""
    SELECT DISTINCT t
    FROM Track t
    JOIN t.artists a
    WHERE a IN :artists
    ORDER BY t.id
""")
  List<Track> findAllByArtistsOrdered(@Param("artists") Set<Artist> artists);

  @Query("""
    SELECT t FROM Track t
    ORDER BY FUNCTION('RAND')
    """)
  List<Track> findRandomTracks(Pageable pageable);

  List<Track> findTracksByIdIn(Set<UUID> ids);
}
