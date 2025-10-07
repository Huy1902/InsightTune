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

/**
 * Controller xử lý các API liên quan đến xác thực người dùng.
 * <p>
 * Bao gồm các chức năng:
 * <ul>
 *     <li>Đăng ký (register)</li>
 *     <li>Đăng nhập (login)</li>
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
     * API đăng ký người dùng mới.
     *
     * @param registerRequest thông tin đăng ký (email, password, firstname, lastname, ...)
     * @return ApiResponse chứa thông tin user vừa được tạo
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
     * API đăng nhập bằng email và password.
     *
     * @param loginRequest thông tin login (email, password)
     * @return ApiResponse chứa thông tin xác thực, access token và refresh token
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
     * API refresh access token.
     *
     * @param request thông tin refresh token
     * @return ApiResponse chứa access token mới
     * @throws ParseException nếu token không parse được
     * @throws JOSEException  nếu token không hợp lệ
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
     * API logout người dùng.
     *
     * @param logoutRequest thông tin refresh token và access token
     * @return ApiResponse xác nhận logout
     * @throws ParseException nếu token không parse được
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "API logout bằng refreshToken ở body và accessToken header")
    public ApiResponse<AuthenticationResponse> logout(@RequestBody LogoutRequest logoutRequest) throws ParseException {
        authService.logout(logoutRequest);
        return ApiResponse.<AuthenticationResponse>builder()
                .code(200)
                .build();
    }

}
