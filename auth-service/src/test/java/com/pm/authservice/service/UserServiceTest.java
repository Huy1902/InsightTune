package com.pm.authservice.service;

import com.pm.authservice.dto.request.UpdateRoleRequest;
import com.pm.authservice.exception.AppException;
import com.pm.authservice.exception.ErrorCode;
import com.pm.authservice.models.Role;
import com.pm.authservice.repository.RoleRepository;
import com.pm.authservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    // Test 1: Role không tồn tại
    @Test
    void updateRole_whenRoleNotFound_thenThrowException() {
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setEmail("test@example.com");
        request.setRole("ADMIN");

        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
                () -> userService.updateRole(request));
        assertEquals(ErrorCode.ROLE_NOTFOUND, ex.getError());
    }

    // Test 2: Role là USER → không được thay đổi
    @Test
    void updateRole_whenRoleIsUser_thenThrowException() {
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setEmail("test@example.com");
        request.setRole("USER");

        Role userRole = new Role();
        userRole.setName("USER");

        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));

        AppException ex = assertThrows(AppException.class,
                () -> userService.updateRole(request));
        assertEquals(ErrorCode.CANT_CHANGE_ROLE, ex.getError());
    }

    // Test 3: Role hợp lệ → changeRoleByEmail thành công
    @Test
    void updateRole_whenValidRole_thenReturnSuccess() {
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setEmail("test@example.com");
        request.setRole("ADMIN");

        Role adminRole = new Role();
        adminRole.setName("ADMIN");

        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
        when(userRepository.changeRoleByEmail("test@example.com", adminRole)).thenReturn(1);

        int result = userService.updateRole(request);

        assertEquals(1, result);
        verify(userRepository, times(1)).changeRoleByEmail("test@example.com", adminRole);
    }
}
