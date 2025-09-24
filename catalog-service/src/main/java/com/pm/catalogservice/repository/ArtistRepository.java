package com.pm.catalogservice.repository;

import com.pm.catalogservice.model.Artist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;
import java.util.UUID;

public interface ArtistRepository extends JpaRepository<Artist, UUID> {
  Optional<Artist> findByNameIgnoreCase(@RequestParam String name);
}
