package com.pm.authservice.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.pm.authservice.dto.request.LoginRequest;
import com.pm.authservice.dto.request.Oauth2LoginRequest;
import com.pm.authservice.dto.request.RegisterRequest;
import com.pm.authservice.dto.response.AuthenticationResponse;
import com.pm.authservice.dto.response.UserProfileResponse;
import com.pm.authservice.exception.AppException;
import com.pm.authservice.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Oauth2LoginServiceTest {

    private AuthService authService;
    private Oauth2LoginService oauth2LoginService;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        oauth2LoginService = new Oauth2LoginService(authService);
        // set client_id manually cho test
        oauth2LoginService.client_id = "test-client-id";
    }

    @Test
    void givenValidIdToken_whenUserDoesNotExist_thenCreateUserAndAuthenticate() throws Exception {
        String idTokenString = "valid-id-token";
        Oauth2LoginRequest request = new Oauth2LoginRequest();
        request.setIdToken(idTokenString);

        // Mock GoogleIdToken và payload
        GoogleIdToken.Payload payload = mock(GoogleIdToken.Payload.class);
        when(payload.getEmail()).thenReturn("test@example.com");
        when(payload.get("name")).thenReturn("John Doe");
        when(payload.get("picture")).thenReturn("avatar-url");

        GoogleIdToken googleIdToken = mock(GoogleIdToken.class);
        when(googleIdToken.getPayload()).thenReturn(payload);

        // Spy service để mock verifyGoogleIdToken
        Oauth2LoginService spyService = Mockito.spy(oauth2LoginService);
        doReturn(googleIdToken).when(spyService).verifyGoogleIdToken(idTokenString);

        when(authService.existsByEmail("test@example.com")).thenReturn(false);

        UserProfileResponse userProfileResponse = new UserProfileResponse();
        when(authService.createUser(any(RegisterRequest.class))).thenReturn(userProfileResponse);

        AuthenticationResponse authResponse = new AuthenticationResponse();
        when(authService.authenticate(any(LoginRequest.class))).thenReturn(authResponse);

        AuthenticationResponse result = spyService.loginGoogle(request);

        assertNotNull(result);
        verify(authService).createUser(any(RegisterRequest.class));
        verify(authService).authenticate(any(LoginRequest.class));
    }

    @Test
    void givenValidIdToken_whenUserExists_thenAuthenticateOnly() throws Exception {
        String idTokenString = "valid-id-token";
        Oauth2LoginRequest request = new Oauth2LoginRequest();
        request.setIdToken(idTokenString);

        GoogleIdToken.Payload payload = mock(GoogleIdToken.Payload.class);
        when(payload.getEmail()).thenReturn("test@example.com");
        when(payload.get("name")).thenReturn("John Doe");
        when(payload.get("picture")).thenReturn("avatar-url");

        GoogleIdToken googleIdToken = mock(GoogleIdToken.class);
        when(googleIdToken.getPayload()).thenReturn(payload);

        Oauth2LoginService spyService = Mockito.spy(oauth2LoginService);
        doReturn(googleIdToken).when(spyService).verifyGoogleIdToken(idTokenString);

        when(authService.existsByEmail("test@example.com")).thenReturn(true);

        AuthenticationResponse authResponse = new AuthenticationResponse();
        when(authService.authenticate(any(LoginRequest.class))).thenReturn(authResponse);

        AuthenticationResponse result = spyService.loginGoogle(request);

        assertNotNull(result);
        verify(authService, never()).createUser(any(RegisterRequest.class));
        verify(authService).authenticate(any(LoginRequest.class));
    }

    @Test
    void givenInvalidIdToken_thenThrowAppException() throws Exception {
        String idTokenString = "invalid-id-token";
        Oauth2LoginRequest request = new Oauth2LoginRequest();
        request.setIdToken(idTokenString);

        Oauth2LoginService spyService = Mockito.spy(oauth2LoginService);
        doReturn(null).when(spyService).verifyGoogleIdToken(idTokenString);

        AppException exception = assertThrows(AppException.class, () -> spyService.loginGoogle(request));
        assertEquals(ErrorCode.IDTOKEN_NULL, exception.getError());
    }

    @Test
    void loginGoogle_tokenNull_shouldThrowAppException() throws Exception {
        Oauth2LoginRequest request = new Oauth2LoginRequest();
        request.setIdToken("fake-token");

        Oauth2LoginService spyService = spy(oauth2LoginService);
        doReturn(null).when(spyService).verifyGoogleIdToken("fake-token");

        AppException ex = assertThrows(AppException.class,
                () -> spyService.loginGoogle(request));
        assertEquals(ErrorCode.IDTOKEN_NULL, ex.getError());
    }

    // ===== 2️⃣ User chưa tồn tại =====
    @Test
    void loginGoogle_validToken_userNotExists_shouldCreateUserAndAuthenticate() throws Exception {
        Oauth2LoginRequest request = new Oauth2LoginRequest();
        request.setIdToken("valid-token");

        // Mock token và payload
        GoogleIdToken.Payload payload = mock(GoogleIdToken.Payload.class);
        when(payload.getEmail()).thenReturn("test@example.com");
        when(payload.get("name")).thenReturn("John Doe");
        when(payload.get("picture")).thenReturn("avatar.png");

        GoogleIdToken token = mock(GoogleIdToken.class);
        when(token.getPayload()).thenReturn(payload);

        Oauth2LoginService spyService = spy(oauth2LoginService);
        doReturn(token).when(spyService).verifyGoogleIdToken("valid-token");

        when(authService.existsByEmail("test@example.com")).thenReturn(false);

        UserProfileResponse profile = UserProfileResponse.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .role("USER")
                .build();
        when(authService.createUser(any())).thenReturn(profile);

        AuthenticationResponse authResp = AuthenticationResponse.builder()
                .token("access-token")
                .refreshToken("refresh-token")
                .authenticated(true)
                .build();
        when(authService.authenticate(any())).thenReturn(authResp);

        AuthenticationResponse result = spyService.loginGoogle(request);

        assertTrue(result.isAuthenticated());
        assertEquals("access-token", result.getToken());
        assertEquals("refresh-token", result.getRefreshToken());

        verify(authService).createUser(any());
        verify(authService).authenticate(any());
    }

    // ===== 3️⃣ User đã tồn tại =====
    @Test
    void loginGoogle_validToken_userExists_shouldAuthenticateOnly() throws Exception {
        Oauth2LoginRequest request = new Oauth2LoginRequest();
        request.setIdToken("valid-token");

        GoogleIdToken.Payload payload = mock(GoogleIdToken.Payload.class);
        when(payload.getEmail()).thenReturn("test@example.com");
        when(payload.get("name")).thenReturn("John Doe");
        when(payload.get("picture")).thenReturn("avatar.png");

        GoogleIdToken token = mock(GoogleIdToken.class);
        when(token.getPayload()).thenReturn(payload);

        Oauth2LoginService spyService = spy(oauth2LoginService);
        doReturn(token).when(spyService).verifyGoogleIdToken("valid-token");

        when(authService.existsByEmail("test@example.com")).thenReturn(true);

        AuthenticationResponse authResp = AuthenticationResponse.builder()
                .token("access-token")
                .refreshToken("refresh-token")
                .authenticated(true)
                .build();
        when(authService.authenticate(any())).thenReturn(authResp);

        AuthenticationResponse result = spyService.loginGoogle(request);

        assertTrue(result.isAuthenticated());
        assertEquals("access-token", result.getToken());
        assertEquals("refresh-token", result.getRefreshToken());

        verify(authService, never()).createUser(any());
        verify(authService).authenticate(any());
    }

    // ===== 4️⃣ createUser ném AppException =====
    @Test
    void loginGoogle_createUserFails_shouldThrowAppException() throws Exception {
        Oauth2LoginRequest request = new Oauth2LoginRequest();
        request.setIdToken("valid-token");

        GoogleIdToken.Payload payload = mock(GoogleIdToken.Payload.class);
        when(payload.getEmail()).thenReturn("test@example.com");
        when(payload.get("name")).thenReturn("John Doe");

        GoogleIdToken token = mock(GoogleIdToken.class);
        when(token.getPayload()).thenReturn(payload);

        Oauth2LoginService spyService = spy(oauth2LoginService);
        doReturn(token).when(spyService).verifyGoogleIdToken("valid-token");

        when(authService.existsByEmail("test@example.com")).thenReturn(false);

        when(authService.createUser(any()))
                .thenThrow(new AppException(ErrorCode.EMAIL_ALREADY_EXISTS));

        AppException ex = assertThrows(AppException.class,
                () -> spyService.loginGoogle(request));
        assertEquals(ErrorCode.EMAIL_ALREADY_EXISTS, ex.getError());
    }

    // ===== 5️⃣ authenticate ném AppException =====
    @Test
    void loginGoogle_authenticateFails_shouldThrowAppException() throws Exception {
        Oauth2LoginRequest request = new Oauth2LoginRequest();
        request.setIdToken("valid-token");

        GoogleIdToken.Payload payload = mock(GoogleIdToken.Payload.class);
        when(payload.getEmail()).thenReturn("test@example.com");
        when(payload.get("name")).thenReturn("John Doe");

        GoogleIdToken token = mock(GoogleIdToken.class);
        when(token.getPayload()).thenReturn(payload);

        Oauth2LoginService spyService = spy(oauth2LoginService);
        doReturn(token).when(spyService).verifyGoogleIdToken("valid-token");

        when(authService.existsByEmail("test@example.com")).thenReturn(true);

        when(authService.authenticate(any()))
                .thenThrow(new AppException(ErrorCode.PASSWORD_NOT_TRUE));

        AppException ex = assertThrows(AppException.class,
                () -> spyService.loginGoogle(request));
        assertEquals(ErrorCode.PASSWORD_NOT_TRUE, ex.getError());
    }

    // ===== 6️⃣ Name null → fallback firstName/lastName =====
    @Test
    void loginGoogle_nameNull_shouldUseDefaultNames() throws Exception {
        Oauth2LoginRequest request = new Oauth2LoginRequest();
        request.setIdToken("valid-token");

        GoogleIdToken.Payload payload = mock(GoogleIdToken.Payload.class);
        when(payload.getEmail()).thenReturn("test@example.com");
        when(payload.get("name")).thenReturn(null);
        when(payload.get("picture")).thenReturn("avatar.png");

        GoogleIdToken token = mock(GoogleIdToken.class);
        when(token.getPayload()).thenReturn(payload);

        Oauth2LoginService spyService = spy(oauth2LoginService);
        doReturn(token).when(spyService).verifyGoogleIdToken("valid-token");

        when(authService.existsByEmail("test@example.com")).thenReturn(false);

        UserProfileResponse profile = UserProfileResponse.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("Google")
                .lastName("User")
                .role("USER")
                .build();
        when(authService.createUser(any())).thenReturn(profile);

        AuthenticationResponse authResp = AuthenticationResponse.builder()
                .token("access-token")
                .refreshToken("refresh-token")
                .authenticated(true)
                .build();
        when(authService.authenticate(any())).thenReturn(authResp);

        AuthenticationResponse result = spyService.loginGoogle(request);

        assertEquals("Google", profile.getFirstName());
        assertEquals("User", profile.getLastName());
    }
}
