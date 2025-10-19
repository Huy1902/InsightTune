package com.pm.favoriteservice.repository;

import com.pm.favoriteservice.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findAllByEmail(String email);

    @Query("SELECT f.songId FROM Favorite f WHERE f.email = :email")
    Set<UUID> findSongIdsByEmail(@Param("email") String email);

    void deleteByEmailAndSongId(String email, UUID songId);
}
