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
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


import java.text.ParseException;

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


    @PostMapping("/register")
    @Operation(summary = "Register", description = "API register")
    public ApiResponse<UserProfileResponse> register(@Valid @RequestBody RegisterRequest registerRequest){

        UserProfileResponse userProfileResponse = authService.createUser(registerRequest);

        return ApiResponse.<UserProfileResponse>builder()
                .code(200)
                .result(userProfileResponse)
                .build();
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "API login bằng email, password")
    public ApiResponse<AuthenticationResponse> login(@Valid @RequestBody LoginRequest loginRequest){
        var result = authService.authenticate(loginRequest);

        return ApiResponse.<AuthenticationResponse>builder()
                .code(200)
                .result(result)
                .build();
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh accessToken", description = "API refresh")
    public ApiResponse<AuthenticationResponse> refresh(@RequestBody RefreshTokenRequest request) throws ParseException, JOSEException {
        return ApiResponse.<AuthenticationResponse>builder()
                .code(200)
                .result(customTokenService.refreshAccessToken(request.getToken()))
                .build();
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "API logout bằng refreshToken ở body và accessToken header")
    public ApiResponse<AuthenticationResponse> logout(@RequestBody LogoutRequest logoutRequest) throws ParseException {
        authService.logout(logoutRequest);
        return ApiResponse.<AuthenticationResponse>builder()
                .code(200)
                .build();
    }

}
