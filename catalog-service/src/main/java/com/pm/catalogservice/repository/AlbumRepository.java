package com.pm.catalogservice.repository;

import com.pm.catalogservice.model.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;
import java.util.UUID;

public interface AlbumRepository extends JpaRepository<Album, UUID> {
  Optional<Album> findByTitleIgnoreCase(@RequestParam String title);
}
