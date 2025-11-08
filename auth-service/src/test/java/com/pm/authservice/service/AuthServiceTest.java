package com.pm.authservice.service;

import com.pm.authservice.dto.request.LoginRequest;
import com.pm.authservice.dto.request.LogoutRequest;
import com.pm.authservice.dto.request.RegisterRequest;
import com.pm.authservice.dto.response.AuthenticationResponse;
import com.pm.authservice.dto.response.UserProfileResponse;
import com.pm.authservice.exception.AppException;
import com.pm.authservice.exception.ErrorCode;
import com.pm.authservice.models.OneTimePassword;
import com.pm.authservice.models.RefreshToken;
import com.pm.authservice.models.Role;
import com.pm.authservice.models.User;
import com.pm.authservice.repository.OneTimePasswordRepository;
import com.pm.authservice.repository.RefreshTokenRepository;
import com.pm.authservice.repository.RoleRepository;
import com.pm.authservice.repository.UserRepository;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    @Mock
    private MailService mailService;

    @Mock
    private OneTimePasswordRepository oneTimePasswordRepository;

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

        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
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

    // ===== sendOTP =====
    @Test
    void givenNonExistingEmail_whenSendOTP_thenThrowException() {
        String email = "notfound@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(false);

        AppException ex = assertThrows(AppException.class, () -> authService.sendOTP(email));
        assertEquals(ErrorCode.USER_NOTFOUND, ex.getError());
    }

    @Test
    void givenEmail_whenSendOTP_thenCreateNewOTP() throws MessagingException, IOException {
        String email = "test@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);
        when(oneTimePasswordRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Mock mailService
        doNothing().when(mailService).sendMail(anyString(), anyString());

        authService.sendOTP(email);

        verify(oneTimePasswordRepository, times(1)).save(any(OneTimePassword.class));
        verify(mailService, times(1)).sendMail(eq(email), anyString());
    }

    @Test
    void givenEmail_whenSendOTP_thenUpdateExistingOTP() throws MessagingException, IOException {
        String email = "test@example.com";
        OneTimePassword existing = OneTimePassword.builder()
                .email(email)
                .otp("oldOtp")
                .expiry(LocalDateTime.now())
                .otp_used(true)
                .build();

        when(userRepository.existsByEmail(email)).thenReturn(true);
        when(oneTimePasswordRepository.findByEmail(email)).thenReturn(Optional.of(existing));
        doNothing().when(mailService).sendMail(anyString(), anyString());

        authService.sendOTP(email);

        assertFalse(existing.isOtp_used());
        assertNotNull(existing.getOtp());
        verify(oneTimePasswordRepository, times(1)).save(existing);
        verify(mailService, times(1)).sendMail(eq(email), eq(existing.getOtp()));
    }

    @Test
    void givenEmail_whenSendOTPMailFails_thenThrowRuntimeException() throws MessagingException, IOException {
        String email = "test@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);
        when(oneTimePasswordRepository.findByEmail(email)).thenReturn(Optional.empty());
        doThrow(new RuntimeException("SMTP error")).when(mailService).sendMail(anyString(), anyString());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.sendOTP(email));
        assertEquals("SMTP error", ex.getMessage());
    }

    // ===== createUser =====
    @Test
    void givenRoleNotFound_whenCreateUser_thenThrowException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("123456");
        request.setConfirmPassword("123456");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> authService.createUser(request));
        assertEquals(ErrorCode.ROLE_NOTFOUND, ex.getError());
    }

//    @Test
//    void givenRestTemplateFails_whenCreateUser_thenRollbackAndThrow() {
//        RegisterRequest request = new RegisterRequest();
//        request.setEmail("test@example.com");
//        request.setPassword("123456");
//        request.setConfirmPassword("123456");
//        request.setFirstname("John");
//        request.setLastname("Doe");
//
//        Role userRole = new Role();
//        userRole.setName("USER");
//
//        User savedUser = new User();
//        savedUser.setId(1L);
//        savedUser.setEmail("test@example.com");
//
//        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
//        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
//        when(passwordEncoder.encode("123456")).thenReturn("encodedPass");
//        when(userRepository.save(any(User.class))).thenReturn(savedUser);
//        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(savedUser));
//        // mock RestTemplate ném exception
//        doThrow(new RuntimeException("Connection error"))
//                .when(restTemplate)
//                .postForObject(anyString(), any(), eq(Void.class));
//
//        AppException ex = assertThrows(AppException.class, () -> authService.createUser(request));
//        assertEquals(ErrorCode.CANT_CONNECT_USERSERVICE, ex.getError());
//
//        // verify rollback
//        verify(userRepository).delete(savedUser);
//    }

    // ===== logout =====
    @Test
    void givenNonExistingRefreshToken_whenLogout_thenThrowException() {
        LogoutRequest request = new LogoutRequest("invalid-token");
        when(refreshTokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> authService.logout(request));
        assertEquals(ErrorCode.REFRESHTOKEN_INVALID, ex.getError());
    }
}
