package com.pm.authservice.service;

import com.pm.authservice.dto.request.RegisterRequest;
import com.pm.authservice.dto.response.UserProfileResponse;
import com.pm.authservice.models.User;
import com.pm.authservice.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private final AuthService authService;

    public CustomOAuth2UserService(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // Lấy thông tin từ Google
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // Truy cập dữ liệu user từ attributes
        Map<String, Object> attributes = oAuth2User.getAttributes();

        log.info(attributes.toString());
        // Email luôn nằm ở key "email"
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        // Tìm user trong DB, nếu chưa có thì tạo mới
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            RegisterRequest registerRequest = new RegisterRequest();
            registerRequest.setEmail(email);
            registerRequest.setPassword("GOOGLE_LOGIN");
            registerRequest.setConfirmPassword("GOOGLE_LOGIN");

            String str = name;

            // Tách chuỗi thành mảng từ, bỏ bớt khoảng trắng thừa
            String[] parts = str.trim().split("\\s+");
            registerRequest.setLastname(parts[parts.length - 1]);
            if (parts.length >= 2) registerRequest.setFirstname(parts[0]);

            UserProfileResponse userProfileResponse = authService.createUser(registerRequest);
            log.info("userProfileResponse={}", userProfileResponse);

        }
        else {
            log.info(user.toString());
        }
        // Có thể trả về CustomOAuth2User nếu muốn   wrap thêm roles
        return oAuth2User;

    }
}
