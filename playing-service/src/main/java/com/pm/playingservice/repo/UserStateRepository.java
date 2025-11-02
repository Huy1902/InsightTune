package com.pm.playingservice.repo;

import com.pm.playingservice.model.UserState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserStateRepository extends JpaRepository<UserState, UUID> {
  Optional<UserState> findByEmail(String email);
}
