package com.pm.authservice.service;

import com.pm.authservice.dto.request.ChangePasswordRequest;
import com.pm.authservice.dto.request.ForgotPasswordRequest;
import com.pm.authservice.dto.request.UpdateRoleRequest;
import com.pm.authservice.exception.AppException;
import com.pm.authservice.exception.ErrorCode;
import com.pm.authservice.models.OneTimePassword;
import com.pm.authservice.models.Role;
import com.pm.authservice.repository.OneTimePasswordRepository;
import com.pm.authservice.repository.RoleRepository;
import com.pm.authservice.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final OneTimePasswordRepository oneTimePasswordRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder, OneTimePasswordRepository oneTimePasswordRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.oneTimePasswordRepository = oneTimePasswordRepository;
    }

    /**
     * Update the role of a user by email.
     * <p>
     * Changing the role to "USER" is not allowed.
     * </p>
     *
     * @param updateRoleRequest contains email and new role
     * @throws AppException if the role does not exist or cannot be changed
     */
    public void updateRole(UpdateRoleRequest updateRoleRequest) {
        String email = updateRoleRequest.getEmail();
        String roleRequest = updateRoleRequest.getRole();

        Role role = roleRepository.findByName(roleRequest)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOTFOUND));

        if (Objects.equals(role.getName(), "USER")) {
            throw new AppException(ErrorCode.CANT_CHANGE_ROLE);
        }

        userRepository.changeRoleByEmail(email, role);
    }

    /**
     * Change a user's password.
     * <p>
     * Validates the old password before updating. The new password will be hashed before saving to the database.
     * </p>
     *
     * @param changePasswordRequest contains old and new passwords
     * @param email email of the user whose password will be changed
     * @throws AppException if the old password is incorrect
     */
    public void changePassword(ChangePasswordRequest changePasswordRequest, String email) {
        String oldPassword = changePasswordRequest.getOldPassword();
        String userPassword = userRepository.getPasswordByEmail(email);

        boolean authenticate = this.passwordEncoder.matches(
                oldPassword,
                userPassword
        );

        if (!authenticate) {
            throw new AppException(ErrorCode.PASSWORD_NOT_TRUE);
        }

        String hashPassword = passwordEncoder.encode(changePasswordRequest.getNewPassword());
        userRepository.changePasswordByEmail(email, hashPassword);
    }

    /**
     * Reset a user's password using OTP verification.
     *
     * @param forgotPasswordRequest contains email, OTP, new password, and confirm password
     * @throws AppException if passwords do not match or OTP is invalid/expired
     */
    public void forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
        String email = forgotPasswordRequest.getEmail();
        String newPassword = forgotPasswordRequest.getNewPassword();
        String confirmNewPassword = forgotPasswordRequest.getConfirmNewPassword();

        if (!Objects.equals(newPassword, confirmNewPassword)) {
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
        }

        boolean check = true;
        OneTimePassword existingEmail = oneTimePasswordRepository.findByEmail(email).orElse(null);

        if (existingEmail == null
                || !existingEmail.getOtp().equals(forgotPasswordRequest.getOtp())
                || existingEmail.getExpiry().isBefore(LocalDateTime.now())
                || existingEmail.isOtp_used()) {
            check = false;
        }

        if (!check) {
            throw new AppException(ErrorCode.INVALID_OTP);
        }

        String hashPassword = passwordEncoder.encode(newPassword);
        userRepository.changePasswordByEmail(email, hashPassword);

        existingEmail.setOtp_used(true);
        oneTimePasswordRepository.save(existingEmail);
    }
}
