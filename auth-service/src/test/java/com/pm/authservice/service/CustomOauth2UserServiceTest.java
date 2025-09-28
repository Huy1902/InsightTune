package com.pm.authservice.service;

import com.pm.authservice.dto.request.RegisterRequest;
import com.pm.authservice.dto.response.UserProfileResponse;
import com.pm.authservice.models.User;
import com.pm.authservice.repository.RefreshTokenRepository;
import com.pm.authservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomOauth2UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    @Test
    void givenNewUser_whenHandleOAuthUser_thenCreateUser() {
        Map<String, Object> attributes = Map.of(
                "email", "new@example.com",
                "name", "Alice Wonderland"
        );

        OAuth2User mockOAuth2User = mock(OAuth2User.class);
        when(mockOAuth2User.getAttributes()).thenReturn(attributes);

        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());

        UserProfileResponse mockResponse = new UserProfileResponse();
        when(authService.createUser(any(RegisterRequest.class))).thenReturn(mockResponse);

        customOAuth2UserService.handleOAuthUser(mockOAuth2User);

        verify(authService).createUser(argThat(req ->
                req.getEmail().equals("new@example.com") &&
                        req.getFirstname().equals("Alice") &&
                        req.getLastname().equals("Wonderland")
        ));
    }

    @Test
    void givenSingleWordName_whenHandleOAuthUser_thenOnlySetLastname() {
        Map<String, Object> attributes = Map.of(
                "email", "solo@example.com",
                "name", "Solo"
        );

        OAuth2User mockOAuth2User = mock(OAuth2User.class);
        when(mockOAuth2User.getAttributes()).thenReturn(attributes);

        when(userRepository.findByEmail("solo@example.com")).thenReturn(Optional.empty());

        UserProfileResponse mockResponse = new UserProfileResponse();
        when(authService.createUser(any(RegisterRequest.class))).thenReturn(mockResponse);

        customOAuth2UserService.handleOAuthUser(mockOAuth2User);

        verify(authService).createUser(argThat(req ->
                req.getFirstname() == null &&
                        req.getLastname().equals("Solo")
        ));
    }
}
