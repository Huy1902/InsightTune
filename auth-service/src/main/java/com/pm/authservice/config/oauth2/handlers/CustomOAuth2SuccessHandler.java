package com.pm.authservice.config.oauth2.handlers;

import com.pm.authservice.models.RefreshToken;
import com.pm.authservice.models.User;
import com.pm.authservice.repository.RefreshTokenRepository;
import com.pm.authservice.repository.UserRepository;
import com.pm.authservice.service.AuthService;
import com.pm.authservice.service.CustomOAuth2UserService;
import com.pm.authservice.service.CustomTokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final UserRepository userRepository;
    private final CustomTokenService customTokenService;

    public CustomOAuth2SuccessHandler(UserRepository userRepository, CustomTokenService customTokenService) {
        this.userRepository = userRepository;
        this.customTokenService = customTokenService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.debug("onAuthenticationSuccess");
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        User user = userRepository.findByEmail(email).orElse(null);
        String accessToken = customTokenService.generateAccessToken(user);
        String refreshToken = customTokenService.generateRefreshToken(user);


        response.setContentType("application/json");
        response.getWriter().write(
                "{\"token\":\"" + accessToken + "\", \"refreshToken\":\"" + refreshToken + "\"}"
        );
    }
}
