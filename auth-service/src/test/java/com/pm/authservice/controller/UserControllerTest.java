package com.pm.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.authservice.config.JwtAuthenticationFilter;
import com.pm.authservice.dto.request.ChangePasswordRequest;
import com.pm.authservice.dto.request.ForgotPasswordRequest;
import com.pm.authservice.dto.request.UpdateRoleRequest;
import com.pm.authservice.repository.RefreshTokenRepository;
import com.pm.authservice.repository.RoleRepository;
import com.pm.authservice.repository.UserRepository;
import com.pm.authservice.service.AuthService;
import com.pm.authservice.service.CustomTokenService;
import com.pm.authservice.service.MailService;
import com.pm.authservice.service.UserService;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false) // keep security filters (if any) out of the slice
public class UserControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @MockitoBean
    private MailService mailService;

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private AuthService authService;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockitoBean
    private RoleRepository roleRepository;
    @MockitoBean
    private CustomTokenService customTokenService;
    @MockitoBean
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    void givenValidInput_whenUpdateRole_then200AndReturnsDtoJson() throws Exception {
        UpdateRoleRequest updateRoleRequest = UpdateRoleRequest.builder()
                .email("test@example.com")
                .role("ADMIN")
                .build();

        // vì userService.updateRole() là void nên mock doNothing()
        doNothing().when(userService).updateRole(any(UpdateRoleRequest.class));

        mockMvc.perform(put("/user/updateRole")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateRoleRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.result").value("Update role successful"));
    }

    @Test
    public void givenValidInput_whenChangePassword_then200AndReturnsDtoJson() throws Exception {
        // Prepare request body
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("oldPass123");
        request.setNewPassword("newPass456");

        // Mock userService behavior
        doNothing().when(userService).changePassword(Mockito.any(ChangePasswordRequest.class), Mockito.anyString());

        // Perform PUT request
        mockMvc.perform(put("/user/changePassword")
                        .principal(() -> "testuser@example.com") // mock Principal
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.result").value("Change password successful"));
    }

    @Test
    void givenValidForgotPasswordRequest_whenForgotPassword_thenReturnSuccess() throws Exception {
        // Arrange
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");
        request.setOtp("123456");
        request.setNewPassword("newPass123");
        request.setConfirmNewPassword("newPass123");

        // Giả lập service không ném exception
        doNothing().when(userService).forgotPassword(any(ForgotPasswordRequest.class));

        // Act & Assert
        mockMvc.perform(patch("/user/forgotPassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.result").value("Change password successful"));
    }

    @Test
    void givenTooShortPassword_whenForgotPassword_thenReturnBadRequest() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");
        request.setOtp("123456");
        request.setNewPassword("123"); // quá ngắn
        request.setConfirmNewPassword("123");

        mockMvc.perform(patch("/user/forgotPassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Password must be at least 6 characters"));
    }

    @Test
    void givenEmptyConfirmPassword_whenForgotPassword_thenReturnBadRequest() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");
        request.setOtp("123456");
        request.setNewPassword("newPass123");
        request.setConfirmNewPassword(""); // bỏ trống

        mockMvc.perform(patch("/user/forgotPassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Field must not be blank"));
    }

    @Test
    void givenMissingOtp_whenForgotPassword_thenReturnBadRequest() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");
        request.setOtp(""); // rỗng
        request.setNewPassword("newPass123");
        request.setConfirmNewPassword("newPass123");

        mockMvc.perform(patch("/user/forgotPassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Field must not be blank"));
    }
}
