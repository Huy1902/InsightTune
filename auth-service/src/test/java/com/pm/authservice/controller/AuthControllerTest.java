package com.pm.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.authservice.config.JwtAuthenticationFilter;
import com.pm.authservice.dto.request.RegisterRequest;
import com.pm.authservice.dto.response.UserProfileResponse;
import com.pm.authservice.exception.AppException;
import com.pm.authservice.exception.ErrorCode;
import com.pm.authservice.repository.RefreshTokenRepository;
import com.pm.authservice.repository.RoleRepository;
import com.pm.authservice.repository.UserRepository;
import com.pm.authservice.service.AuthService;
import com.pm.authservice.service.CustomTokenService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // keep security filters (if any) out of the slice
public class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    AuthService authService;
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
    void givenValidInput_whenCreateUser_then200AndReturnsDtoJson() throws Exception {
        RegisterRequest  registerRequest =  RegisterRequest.builder()
                                            .email("test@example.com")
                                            .firstname("Tran")
                                            .lastname("Dinh")
                                            .password("123456")
                                            .confirmPassword("123456").build();


        UserProfileResponse mockResponse = UserProfileResponse.builder()
                                            .id(1L)
                                            .email("test@example.com")
                                            .role("USER")
                                            .fullName("Tran Dinh")
                                            .build();

        when(authService.createUser(any(RegisterRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.email").value("test@example.com"))
                .andExpect(jsonPath("$.result.role").value("USER"))
                .andExpect(jsonPath("$.result.fullName").value("Tran Dinh"))
                .andExpect(jsonPath("$.result.id").value(1L));

    }

    @Test
    void givenInvalidPassword_whenCreateUser_then400AndReturnsDtoJson() throws Exception {
        RegisterRequest  registerRequest =  RegisterRequest.builder()
                .email("test@example.com")
                .firstname("Tran")
                .lastname("Dinh")
                .password("21")
                .confirmPassword("123456").build();

        when(authService.createUser(any(RegisterRequest.class)))
                .thenThrow(new AppException(ErrorCode.PASSWORD_INVALID));

        mockMvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Password must be at least 6 characters"));

    }

    @Test
    void givenInvalidEmail_whenCreateUser_then400AndReturnsDtoJson() throws Exception {
        RegisterRequest  registerRequest =  RegisterRequest.builder()
                .email("testexample.com")
                .firstname("Tran")
                .lastname("Dinh")
                .password("123456")
                .confirmPassword("123456").build();


        when(authService.createUser(any(RegisterRequest.class)))
                .thenThrow(new AppException(ErrorCode.EMAIL_INVALID));

        mockMvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Invalid email"));

    }

    @Test
    void givenEmptyLastName_whenCreateUser_then400AndReturnsDtoJson() throws Exception {
        RegisterRequest  registerRequest =  RegisterRequest.builder()
                .email("testexample.com")
                .firstname("Tran")
                .lastname("")
                .password("123456")
                .confirmPassword("123456").build();

        when(authService.createUser(any(RegisterRequest.class)))
                .thenThrow(new AppException(ErrorCode.NOT_BLANK));

        mockMvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Field must not be blank"));

    }

    @Test
    void givenEmptyFirstName_whenCreateUser_then400AndReturnsDtoJson() throws Exception {
        RegisterRequest  registerRequest =  RegisterRequest.builder()
                .email("testexample.com")
                .firstname("")
                .lastname("Dinh")
                .password("123456")
                .confirmPassword("123456").build();


        when(authService.createUser(any(RegisterRequest.class)))
                .thenThrow(new AppException(ErrorCode.NOT_BLANK));

        mockMvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Field must not be blank"));

    }

}
