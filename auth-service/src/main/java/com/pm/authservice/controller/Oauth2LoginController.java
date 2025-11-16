package com.pm.authservice.controller;

import com.pm.authservice.dto.request.Oauth2LoginRequest;
import com.pm.authservice.dto.response.ApiResponse;
import com.pm.authservice.dto.response.AuthenticationResponse;
import com.pm.authservice.service.Oauth2LoginService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Controller that handles OAuth2 login APIs.
 * <p>
 * Currently supports login via Google.
 * </p>
 */
@RestController
@RequestMapping("/api/auth")
public class Oauth2LoginController {
    private final Oauth2LoginService oauth2LoginService;

    public Oauth2LoginController(Oauth2LoginService oauth2LoginService) {
        this.oauth2LoginService = oauth2LoginService;
    }

    /**
     * API for logging in using Google OAuth2.
     * <p>
     * The client sends the ID token received from Google; the server validates the token and logs in the user.
     * If the user does not exist, a new account will be automatically created.
     * </p>
     *
     * @param request OAuth2 login information (contains Google ID token)
     * @return ApiResponse containing AuthenticationResponse with access token and user information
     * @throws GeneralSecurityException if the Google token is invalid
     * @throws IOException              if an I/O error occurs during token validation
     */
    @PostMapping("/google")
    public ApiResponse<AuthenticationResponse> loginWithGoogle(@RequestBody Oauth2LoginRequest request) throws GeneralSecurityException, IOException {
        return ApiResponse.<AuthenticationResponse>builder()
                .code(200)
                .result(oauth2LoginService.loginGoogle(request))
                .build();
    }
}
