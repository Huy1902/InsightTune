package com.pm.userservice.repository;

import com.pm.userservice.models.Role;
import com.pm.userservice.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    void deleteById(Long id);

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);
}
