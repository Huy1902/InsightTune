package com.pm.authservice.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.pm.authservice.dto.request.LoginRequest;
import com.pm.authservice.dto.request.Oauth2LoginRequest;
import com.pm.authservice.dto.request.RegisterRequest;
import com.pm.authservice.dto.response.AuthenticationResponse;
import com.pm.authservice.dto.response.UserProfileResponse;
import com.pm.authservice.exception.AppException;
import com.pm.authservice.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;

/**
 * Service handling OAuth2 login with Google.
 * <p>
 * Includes the following functionalities:
 * <ul>
 *     <li>Verify Google ID Token</li>
 *     <li>Login using Google</li>
 *     <li>Automatically create a new user if the email does not exist</li>
 * </ul>
 * </p>
 */
@Service
@Slf4j
public class Oauth2LoginService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    String client_id;

    private final AuthService authService;

    public Oauth2LoginService(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Verify Google ID Token from the client.
     *
     * @param idTokenString the ID token string from Google
     * @return a verified GoogleIdToken, or null if invalid
     * @throws GeneralSecurityException if security verification fails
     * @throws IOException              if an I/O error occurs
     */
    protected GoogleIdToken verifyGoogleIdToken(String idTokenString) throws GeneralSecurityException, IOException {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                JacksonFactory.getDefaultInstance()
        )
                .setAudience(Collections.singletonList(client_id))
                .build();

        return verifier.verify(idTokenString);
    }

    /**
     * Login using Google OAuth2.
     * <p>
     * If the user does not exist, a new user will be automatically created with the default password "GOOGLE_LOGIN".
     * Then the user is authenticated and receives an access token and refresh token.
     * </p>
     *
     * @param request Oauth2LoginRequest containing the Google ID token
     * @return AuthenticationResponse containing access token, refresh token, and authenticated status
     * @throws GeneralSecurityException if token verification fails
     * @throws IOException              if an I/O error occurs
     * @throws AppException             if the ID token is null or invalid
     */
    public AuthenticationResponse loginGoogle(Oauth2LoginRequest request) throws GeneralSecurityException, IOException {
        String idTokenString = request.getIdToken();

        GoogleIdToken idToken = verifyGoogleIdToken(idTokenString);
        if (idToken == null) {
            throw new AppException(ErrorCode.IDTOKEN_NULL);
        }

        GoogleIdToken.Payload payload = idToken.getPayload();

        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String picture = (String) payload.get("picture");

        boolean checkUser = authService.existsByEmail(email);
        if (!checkUser) {
            String[] parts = (name != null ? name.trim().split("\\s+") : new String[0]);

            String firstName = parts.length > 1
                    ? String.join(" ", Arrays.copyOf(parts, parts.length - 1))
                    : "Google";
            String lastName = parts.length > 1 ? parts[parts.length - 1] : "User";

            RegisterRequest registerRequest = RegisterRequest.builder()
                    .email(email)
                    .firstname(firstName)
                    .lastname(lastName)
                    .password("GOOGLE_LOGIN")
                    .confirmPassword("GOOGLE_LOGIN")
                    .avatar(picture)
                    .build();

            UserProfileResponse userProfileResponse = authService.createUser(registerRequest);
            log.info("userProfileResponse={}", userProfileResponse);
        }

        LoginRequest loginRequest = LoginRequest.builder()
                .email(email)
                .password("GOOGLE_LOGIN")
                .build();

        return authService.authenticate(loginRequest);
    }
}
