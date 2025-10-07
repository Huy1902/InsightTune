package com.pm.authservice.repository;

import com.pm.authservice.models.Role;
import com.pm.authservice.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    /**
     * Cập nhật role của user dựa trên email.
     *
     * @param email email của người dùng
     * @param role  role mới cần cập nhật
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.role = :role WHERE u.email = :email")
    void changeRoleByEmail(@Param("email") String email, @Param("role") Role role);

    /**
     * Lấy mật khẩu của user theo email.
     *
     * @param email email của người dùng
     * @return mật khẩu của user dưới dạng String
     */
    @Query("SELECT u.password FROM User u WHERE u.email = :email")
    String getPasswordByEmail(String email);


    /**
     * Cập nhật mật khẩu của user dựa trên email.
     *
     * @param email       email của người dùng
     * @param newPassword mật khẩu mới cần cập nhật
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.password = :newPassword WHERE u.email = :email")
    void changePasswordByEmail(@Param("email") String email, @Param("newPassword") String newPassword);
}
