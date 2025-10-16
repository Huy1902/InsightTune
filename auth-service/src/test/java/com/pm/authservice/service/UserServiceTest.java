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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private OneTimePasswordRepository oneTimePasswordRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

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
    void updateRole_whenValidRole_thenSuccess() {
        // given
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setEmail("test@example.com");
        request.setRole("ADMIN");

        Role adminRole = new Role();
        adminRole.setName("ADMIN");

        when(roleRepository.findByName("ADMIN"))
                .thenReturn(Optional.of(adminRole));

        // when
        userService.updateRole(request);

        // then
        verify(roleRepository, times(1)).findByName("ADMIN");
        verify(userRepository, times(1)).changeRoleByEmail("test@example.com", adminRole);
    }


    @Test
    void testChangePassword_success() {
        // Prepare
        String email = "test@example.com";
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("oldPass123");
        request.setNewPassword("newPass456");

        String encodedOldPassword = "$2a$10$encodedOldPassword"; // giả lập hashed password

        when(userRepository.getPasswordByEmail(email)).thenReturn(encodedOldPassword);
        when(passwordEncoder.matches("oldPass123", encodedOldPassword)).thenReturn(true);
        when(passwordEncoder.encode("newPass456")).thenReturn("$2a$10$encodedNewPassword");

        // Call method
        userService.changePassword(request, email);

        // Verify
        verify(userRepository).changePasswordByEmail(email, "$2a$10$encodedNewPassword");
    }

    @Test
    void testChangePassword_wrongOldPassword() {
        // Prepare
        String email = "test@example.com";
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("wrongOldPass");
        request.setNewPassword("newPass456");

        String encodedOldPassword = "$2a$10$encodedOldPassword";

        when(userRepository.getPasswordByEmail(email)).thenReturn(encodedOldPassword);
        when(passwordEncoder.matches("wrongOldPass", encodedOldPassword)).thenReturn(false);

        // Call method and assert exception
        AppException exception = assertThrows(AppException.class, () -> {
            userService.changePassword(request, email);
        });

        assertEquals(ErrorCode.PASSWORD_NOT_TRUE, exception.getError());

        // Verify repository method never called
        verify(userRepository, never()).changePasswordByEmail(anyString(), anyString());
    }


    @Test
    void getValidOtp_whenForgotPassword_thenSuccess() {
        // given
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@gmail.com");
        request.setOtp("123456");
        request.setNewPassword("newPass123");
        request.setConfirmNewPassword("newPass123");

        OneTimePassword otp = new OneTimePassword();
        otp.setEmail("test@gmail.com");
        otp.setOtp("123456");
        otp.setExpiry(LocalDateTime.now().plusMinutes(5));
        otp.setOtp_used(false);

        when(oneTimePasswordRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(otp));
        when(passwordEncoder.encode("newPass123")).thenReturn("hashed_pw");

        // when
        userService.forgotPassword(request);

        // then
        verify(passwordEncoder).encode("newPass123");
        verify(userRepository).changePasswordByEmail("test@gmail.com", "hashed_pw");
        verify(oneTimePasswordRepository).save(otp);
        assertTrue(otp.isOtp_used());
    }

    @Test
    void getPasswordsDoNotMatch_whenForgotPassword_thenThrowException() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@gmail.com");
        request.setOtp("123456");
        request.setNewPassword("newPass123");
        request.setConfirmNewPassword("wrongPass");

        AppException ex = assertThrows(AppException.class, () ->
                userService.forgotPassword(request));

        assertEquals(ErrorCode.PASSWORD_NOT_MATCH, ex.getError());
        verify(userRepository, never()).changePasswordByEmail(anyString(), anyString());
    }

    @Test
    void getOtpNotFound_whenForgotPassword_thenThrowException() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@gmail.com");
        request.setOtp("123456");
        request.setNewPassword("newPass123");
        request.setConfirmNewPassword("newPass123");

        when(oneTimePasswordRepository.findByEmail("test@gmail.com")).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () ->
                userService.forgotPassword(request));

        assertEquals(ErrorCode.INVALID_OTP, ex.getError());
    }

    @Test
    void getOtpWrong_whenForgotPassword_thenThrowException() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@gmail.com");
        request.setOtp("999999"); // khác OTP
        request.setNewPassword("newPass123");
        request.setConfirmNewPassword("newPass123");

        OneTimePassword otp = new OneTimePassword();
        otp.setEmail("test@gmail.com");
        otp.setOtp("123456");
        otp.setExpiry(LocalDateTime.now().plusMinutes(5));
        otp.setOtp_used(false);

        when(oneTimePasswordRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(otp));

        AppException ex = assertThrows(AppException.class, () ->
                userService.forgotPassword(request));

        assertEquals(ErrorCode.INVALID_OTP, ex.getError());
    }

    @Test
    void getOtpExpired_whenForgotPassword_thenThrowException() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@gmail.com");
        request.setOtp("123456");
        request.setNewPassword("newPass123");
        request.setConfirmNewPassword("newPass123");

        OneTimePassword otp = new OneTimePassword();
        otp.setEmail("test@gmail.com");
        otp.setOtp("123456");
        otp.setExpiry(LocalDateTime.now().minusMinutes(1)); // hết hạn
        otp.setOtp_used(false);

        when(oneTimePasswordRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(otp));

        AppException ex = assertThrows(AppException.class, () ->
                userService.forgotPassword(request));

        assertEquals(ErrorCode.INVALID_OTP, ex.getError());
    }

    @Test
    void getOtpAlreadyUsed_whenForgotPassword_thenThrowException() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@gmail.com");
        request.setOtp("123456");
        request.setNewPassword("newPass123");
        request.setConfirmNewPassword("newPass123");

        OneTimePassword otp = new OneTimePassword();
        otp.setEmail("test@gmail.com");
        otp.setOtp("123456");
        otp.setExpiry(LocalDateTime.now().plusMinutes(5));
        otp.setOtp_used(true); // đã dùng

        when(oneTimePasswordRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(otp));

        AppException ex = assertThrows(AppException.class, () ->
                userService.forgotPassword(request));

        assertEquals(ErrorCode.INVALID_OTP, ex.getError());
    }

}
