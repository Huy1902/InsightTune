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
import jakarta.validation.Valid;
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
     * Cập nhật role của user theo email.
     * <p>
     * Không cho phép chuyển role về "USER".
     * </p>
     *
     * @param updateRoleRequest chứa email và role mới
     * @throws AppException nếu role không tồn tại hoặc không thể thay đổi
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
     * Thay đổi mật khẩu user.
     * <p>
     * Xác thực mật khẩu cũ trước khi thay đổi. Mật khẩu mới sẽ được mã hóa trước khi lưu vào database.
     * </p>
     *
     * @param changePasswordRequest chứa mật khẩu cũ và mật khẩu mới
     * @param email email của user cần thay đổi mật khẩu
     * @throws AppException nếu mật khẩu cũ không đúng
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
