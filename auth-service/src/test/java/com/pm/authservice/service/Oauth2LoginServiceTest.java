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
}
