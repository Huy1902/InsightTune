package com.pm.authservice.controller;

import com.pm.authservice.dto.request.RegisterRequest;
import com.pm.authservice.dto.response.ApiResponse;
import com.pm.authservice.dto.response.UserProfileResponse;
import com.pm.authservice.models.User;
import com.pm.authservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final RestClient.Builder builder;
    private final RestTemplate restTemplate;

    public AuthController(PasswordEncoder passwordEncoder, UserService userService, RestClient.Builder builder, RestTemplate restTemplate) {
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.builder = builder;
        this.restTemplate = restTemplate;
    }


    @PostMapping("/register")
    public ApiResponse<UserProfileResponse> register(@Valid @RequestBody RegisterRequest registerRequest){
        User user = new  User();

        // luu user vao db
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        userService.save(user);

        // lay thong tin userprofile
        UserProfileResponse userProfileResponse = new UserProfileResponse();
        userProfileResponse.setId(userService.findByEmail(registerRequest.getEmail()).getId());
        userProfileResponse.setFullName(registerRequest.getFirstname()
                + " " + registerRequest.getLastname());
        userProfileResponse.setEmail(registerRequest.getEmail());

        // Gọi sang User Service để tạo profile
        String url = "http://localhost:8081/users/create";
        restTemplate.postForObject(url, userProfileResponse, Void.class);

        return ApiResponse.<UserProfileResponse>builder()
                .code(200)
                .result(userProfileResponse)
                .build();
    }


}
