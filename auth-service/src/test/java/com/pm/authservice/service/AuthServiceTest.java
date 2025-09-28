package com.pm.authservice.service;

import com.pm.authservice.dto.request.LoginRequest;
import com.pm.authservice.dto.request.LogoutRequest;
import com.pm.authservice.dto.request.RegisterRequest;
import com.pm.authservice.dto.response.AuthenticationResponse;
import com.pm.authservice.dto.response.UserProfileResponse;
import com.pm.authservice.exception.AppException;
import com.pm.authservice.exception.ErrorCode;
import com.pm.authservice.models.RefreshToken;
import com.pm.authservice.models.Role;
import com.pm.authservice.models.User;
import com.pm.authservice.repository.RefreshTokenRepository;
import com.pm.authservice.repository.RoleRepository;
import com.pm.authservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomTokenService tokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void givenValidInput_whenAuthenticate_then200AndReturnsDtoJson() {
        User mockUser = new User();
        mockUser.setEmail("test@example.com");
        mockUser.setPassword("encodedPass");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("123456", "encodedPass")).thenReturn(true);
        when(tokenService.generateAccessToken(mockUser)).thenReturn("access-token");
        when(tokenService.generateRefreshToken(mockUser)).thenReturn("refresh-token");

        LoginRequest loginRequest = new LoginRequest("test@example.com", "123456");

        AuthenticationResponse response = authService.authenticate(loginRequest);

        assertTrue(response.isAuthenticated());
        assertEquals("access-token", response.getToken());
        assertEquals("refresh-token", response.getRefreshToken());
    }

    @Test
    void givenWrongPassword_whenAuthenticate_thenThrowException() {
        User mockUser = new User();
        mockUser.setEmail("test@example.com");
        mockUser.setPassword("encodedPass");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrong", "encodedPass")).thenReturn(false);

        LoginRequest loginRequest = new LoginRequest("test@example.com", "wrong");

        AppException ex = assertThrows(AppException.class,
                () -> authService.authenticate(loginRequest));
        assertEquals(ErrorCode.PASSWORD_NOT_TRUE, ex.getError());
    }

    @Test
    void givenInvalidEmail_whenAuthenticate_thenThrowException() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("notexist@example.com");
        loginRequest.setPassword("123456");

        // Giả lập repository trả về Optional.empty() → email không tồn tại
        when(userRepository.findByEmail("notexist@example.com")).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
                () -> authService.authenticate(loginRequest));

        // Kiểm tra mã lỗi
        assertEquals(ErrorCode.USER_NOTFOUND, ex.getError());
    }

    @Test
    void givenValidInput_whenCreateUser_then200AndReturnUserProfileResponse() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("123456");
        request.setConfirmPassword("123456");
        request.setFirstname("John");
        request.setLastname("Doe");

        Role userRole = new Role();
        userRole.setName("USER");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("test@example.com");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("123")).thenReturn("encoded123");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(savedUser));

        UserProfileResponse response = authService.createUser(request);

        assertEquals("John Doe", response.getFullName());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("USER", response.getRole());
        assertEquals(1L, response.getId());
    }

    @Test
    void givenExistingEmail_whenCreateUser_thenThrowException() {
        RegisterRequest req = RegisterRequest.builder()
                .email("test@example.com")
                .password("123456")
                .confirmPassword("123456")
                .firstname("firstname")
                .lastname("lastname")
                .build();

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        AppException ex = assertThrows(AppException.class, () -> authService.createUser(req));
        assertEquals(ErrorCode.EMAIL_ALREADY_EXISTS, ex.getError());
    }

    @Test
    void givenPasswordNotMatch_whenCreateUser_thenThrowException() {
        RegisterRequest req = RegisterRequest.builder()
                .email("test@example.com")
                .password("123456")
                .confirmPassword("654321")
                .build();

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);

        AppException ex = assertThrows(AppException.class, () -> authService.createUser(req));
        assertEquals(ErrorCode.PASSWORD_NOT_MATCH, ex.getError());
    }

    @Test
    void givenValid_whenLogout_thenDeleteRefreshToken() {
        LogoutRequest request = new LogoutRequest("refresh-token");
        RefreshToken token = new RefreshToken();
        token.setToken("refresh-token");

        when(refreshTokenRepository.findByToken("refresh-token")).thenReturn(Optional.of(token));

        authService.logout(request);

        verify(refreshTokenRepository).delete(token);
    }
}
