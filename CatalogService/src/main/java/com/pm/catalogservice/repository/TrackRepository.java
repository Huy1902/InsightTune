package com.pm.catalogservice.repository;

import com.pm.catalogservice.model.Track;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TrackRepository extends JpaRepository<Track, UUID> {
}
