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
 * Controller xử lý các API đăng nhập OAuth2.
 * <p>
 * Hiện tại hỗ trợ đăng nhập bằng Google.
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
     * API đăng nhập bằng Google OAuth2.
     * <p>
     * Client gửi ID token nhận được từ Google, server xác thực token và đăng nhập người dùng.
     * Nếu người dùng chưa tồn tại, sẽ tự động tạo tài khoản mới.
     * </p>
     *
     * @param request thông tin OAuth2 login (chứa Google ID token)
     * @return ApiResponse chứa AuthenticationResponse với access token và thông tin user
     * @throws GeneralSecurityException nếu token Google không hợp lệ
     * @throws IOException              nếu có lỗi IO khi xác thực token
     */
    @PostMapping("/google")
    public ApiResponse<AuthenticationResponse> loginWithGoogle(@RequestBody Oauth2LoginRequest request) throws GeneralSecurityException, IOException {
        return ApiResponse.<AuthenticationResponse>builder()
                .code(200)
                .result(oauth2LoginService.loginGoogle(request))
                .build();
    }
}
