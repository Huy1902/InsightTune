package com.pm.authservice.service;

import com.pm.authservice.dto.request.ChangePasswordRequest;
import com.pm.authservice.dto.request.UpdateRoleRequest;
import com.pm.authservice.exception.AppException;
import com.pm.authservice.exception.ErrorCode;
import com.pm.authservice.models.Role;
import com.pm.authservice.repository.RoleRepository;
import com.pm.authservice.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

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

}
