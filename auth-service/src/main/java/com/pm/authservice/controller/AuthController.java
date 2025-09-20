package com.pm.authservice.controller;

import com.nimbusds.jose.JOSEException;
import com.pm.authservice.dto.request.LoginRequest;
import com.pm.authservice.dto.request.LogoutRequest;
import com.pm.authservice.dto.request.RefreshTokenRequest;
import com.pm.authservice.dto.request.RegisterRequest;
import com.pm.authservice.dto.response.ApiResponse;
import com.pm.authservice.dto.response.AuthenticationResponse;
import com.pm.authservice.dto.response.UserProfileResponse;
import com.pm.authservice.exception.AppException;
import com.pm.authservice.exception.ErrorCode;
import com.pm.authservice.models.User;
import com.pm.authservice.repository.RoleRepository;
import com.pm.authservice.service.AuthService;
import com.pm.authservice.service.CustomTokenService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    @Value("${user-service.create-path}")
    private String createUserPath;

    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final RestTemplate restTemplate;
    private final RoleRepository roleRepository;
    private final CustomTokenService customTokenService;

    public AuthController(PasswordEncoder passwordEncoder, AuthService authService, RestTemplate restTemplate, RestTemplate restTemplate1, RoleRepository roleRepository, CustomTokenService customTokenService) {
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
        this.restTemplate = restTemplate1;
        this.roleRepository = roleRepository;
        this.customTokenService = customTokenService;
    }


    @PostMapping("/register")
    public ApiResponse<UserProfileResponse> register(@Valid @RequestBody RegisterRequest registerRequest){
        log.info("register api");
        User user = new  User();

        if (authService.existsByEmail(registerRequest.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
        }

        // luu user vao db
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        // lay role tu form
        user.setRole(roleRepository.findByName("USER").orElseThrow(()
                -> new AppException(ErrorCode.ROLE_NOTFOUND)));
        authService.save(user);

        // lay thong tin userprofile
        UserProfileResponse userProfileResponse = new UserProfileResponse();
        userProfileResponse.setId(authService.findByEmail(registerRequest.getEmail()).getId());
        userProfileResponse.setFullName(registerRequest.getFirstname()
                + " " + registerRequest.getLastname());
        userProfileResponse.setEmail(registerRequest.getEmail());

        String url = createUserPath;
        restTemplate.postForObject(url, userProfileResponse, Void.class);

        return ApiResponse.<UserProfileResponse>builder()
                .code(200)
                .result(userProfileResponse)
                .build();
    }

    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> login(@Valid @RequestBody LoginRequest loginRequest){
        var result = authService.authenticate(loginRequest);

        return ApiResponse.<AuthenticationResponse>builder()
                .code(200)
                .result(result)
                .build();
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthenticationResponse> refresh(@RequestBody RefreshTokenRequest request) throws ParseException, JOSEException {
        return ApiResponse.<AuthenticationResponse>builder()
                .code(200)
                .result(customTokenService.refreshAccessToken(request.getToken()))
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<AuthenticationResponse> logout(@RequestBody LogoutRequest logoutRequest) throws ParseException {
        authService.logout(logoutRequest);
        return ApiResponse.<AuthenticationResponse>builder()
                .code(200)
                .build();
    }
}
