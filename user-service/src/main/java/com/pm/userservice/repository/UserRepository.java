package com.pm.userservice.repository;

import com.pm.userservice.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    void deleteById(Long id);

    Optional<User> findByEmail(String email);

    void deleteByEmail(String email);

    /**
     * Update the user's avatar by email.
     *
     * @param email the email of the user whose avatar needs to be changed
     * @param avatar the new avatar link or file name
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.avatar = :avatar WHERE u.email = :email")
    void changeAvatarByEmail(@Param("email") String email, @Param("avatar") String avatar);
}
