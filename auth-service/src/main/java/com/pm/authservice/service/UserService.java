package com.pm.authservice.service;

import com.pm.authservice.dto.request.UpdateRoleRequest;
import com.pm.authservice.dto.response.UserProfileResponse;
import com.pm.authservice.exception.AppException;
import com.pm.authservice.exception.ErrorCode;
import com.pm.authservice.models.Role;
import com.pm.authservice.models.User;
import com.pm.authservice.repository.RoleRepository;
import com.pm.authservice.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public int updateRole(UpdateRoleRequest updateRoleRequest) {
        String email = updateRoleRequest.getEmail();
        String roleRequest = updateRoleRequest.getRole();

        Role role = roleRepository.findByName(roleRequest)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOTFOUND));

        if (Objects.equals(role.getName(), "USER")) {
            throw new AppException(ErrorCode.CANT_CHANGE_ROLE);
        }

        return userRepository.changeRoleByEmail(email, role);
    }
}
