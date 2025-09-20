package com.pm.authservice.service;

import com.pm.authservice.dto.request.LoginRequest;
import com.pm.authservice.dto.request.LogoutRequest;
import com.pm.authservice.dto.response.AuthenticationResponse;
import com.pm.authservice.exception.AppException;
import com.pm.authservice.exception.ErrorCode;
import com.pm.authservice.models.RefreshToken;
import com.pm.authservice.models.User;
import com.pm.authservice.repository.RefreshTokenRepository;
import com.pm.authservice.repository.UserRepository;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthService {
    @NonFinal
    @Value("${jwt.signerKey}")
    String SIGNER_KEY;

    private final UserRepository userRepository;
    private final CustomTokenService tokenService;

    private final RefreshTokenRepository refreshTokenRepository;

    public AuthService(UserRepository userRepository, CustomTokenService tokenService, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public void save(User user) {
        userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(()
                -> new AppException(ErrorCode.USER_NOTFOUND));
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public AuthenticationResponse authenticate(LoginRequest authenticationRequest) {
        // find user
        User user = userRepository.findByEmail(authenticationRequest.getEmail())
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOTFOUND));

        // authenticate password
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        boolean authenticate = passwordEncoder.matches(authenticationRequest.getPassword()
                , user.getPassword());
        if (!authenticate) {
            throw new AppException(ErrorCode.PASSWORD_NOT_TRUE);
        }

        // accessToken
        var token = tokenService.generateAccessToken(user);

        // luu refreshToken vao db
        String refreshToken = tokenService.generateRefreshToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .authenticated(true)
                .build();
    }

    public void logout(LogoutRequest request){

        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getToken()).orElseThrow(
                () -> new AppException(ErrorCode.REFRESHTOKEN_INVALID)
        );

        refreshTokenRepository.delete(refreshToken);

    }
}
