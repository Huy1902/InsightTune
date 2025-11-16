package com.pm.authservice.controller;

import com.nimbusds.jose.JOSEException;
import com.pm.authservice.dto.request.LoginRequest;
import com.pm.authservice.dto.request.LogoutRequest;
import com.pm.authservice.dto.request.RefreshTokenRequest;
import com.pm.authservice.dto.request.RegisterRequest;
import com.pm.authservice.dto.response.ApiResponse;
import com.pm.authservice.dto.response.AuthenticationResponse;
import com.pm.authservice.dto.response.UserProfileResponse;
import com.pm.authservice.service.AuthService;
import com.pm.authservice.service.CustomTokenService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

/**
 * Controller that handles APIs related to user authentication.
 * <p>
 * Includes the following functionalities:
 * <ul>
 *     <li>User registration (register)</li>
 *     <li>User login</li>
 *     <li>Refresh access token</li>
 *     <li>Logout</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final CustomTokenService customTokenService;

    public AuthController( AuthService authService, CustomTokenService customTokenService) {
        this.authService = authService;
        this.customTokenService = customTokenService;
    }

    /**
     * API for registering a new user.
     *
     * @param registerRequest registration information (email, password, firstname, lastname, etc.)
     * @return ApiResponse containing the information of the newly created user
     */
    @PostMapping("/register")
    @Operation(summary = "Register", description = "API register")
    public ApiResponse<UserProfileResponse> register(@Valid @RequestBody RegisterRequest registerRequest){

        UserProfileResponse userProfileResponse = authService.createUser(registerRequest);

        return ApiResponse.<UserProfileResponse>builder()
                .code(200)
                .result(userProfileResponse)
                .build();
    }


    /**
     * API for logging in using email and password.
     *
     * @param loginRequest login information (email, password)
     * @return ApiResponse containing authentication details, access token, and refresh token
     */
    @PostMapping("/login")
    @Operation(summary = "Login", description = "API login bằng email, password")
    public ApiResponse<AuthenticationResponse> login(@Valid @RequestBody LoginRequest loginRequest){
        var result = authService.authenticate(loginRequest);

        return ApiResponse.<AuthenticationResponse>builder()
                .code(200)
                .result(result)
                .build();
    }

    /**
     * API to refresh the access token.
     *
     * @param request refresh token information
     * @return ApiResponse containing the new access token
     * @throws ParseException if the token cannot be parsed
     * @throws JOSEException  if the token is invalid
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh accessToken", description = "API refresh, need accessToken")
    public ApiResponse<AuthenticationResponse> refresh(@RequestBody RefreshTokenRequest request) throws ParseException, JOSEException {
        return ApiResponse.<AuthenticationResponse>builder()
                .code(200)
                .result(customTokenService.refreshAccessToken(request.getToken()))
                .build();
    }

    /**
     * API for logging out a user.
     *
     * @param logoutRequest information including refresh token and access token
     * @return ApiResponse confirming the logout
     * @throws ParseException if the token cannot be parsed
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "API logout bằng refreshToken ở body và accessToken header")
    public ApiResponse<AuthenticationResponse> logout(@RequestBody LogoutRequest logoutRequest) throws ParseException {
        authService.logout(logoutRequest);
        return ApiResponse.<AuthenticationResponse>builder()
                .code(200)
                .build();
    }

    /**
     * API to send an OTP to the user's email when they forget their password.
     *
     * @param email the user's email address
     * @return ApiResponse indicating that the OTP has been sent
     */
    @PostMapping("/forgot_password")
    @Operation(summary = "Forgot password", description = "API for to get otp in email")
    public ApiResponse<String> sendOTP(@RequestParam String email) {
        authService.sendOTP(email);
        return ApiResponse.<String>builder()
                .code(200)
                .message("OTP has been sent to your email")
                .build();
    }

}
