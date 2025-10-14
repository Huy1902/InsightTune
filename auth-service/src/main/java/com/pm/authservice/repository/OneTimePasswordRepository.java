package com.pm.authservice.repository;

import com.pm.authservice.models.OneTimePassword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OneTimePasswordRepository extends JpaRepository<OneTimePassword,Long> {
    Optional<OneTimePassword> findByEmail(String email);
}
