package com.pm.authservice.service;

import com.pm.authservice.dto.request.LoginRequest;
import com.pm.authservice.dto.request.LogoutRequest;
import com.pm.authservice.dto.request.RegisterRequest;
import com.pm.authservice.dto.response.AuthenticationResponse;
import com.pm.authservice.dto.response.UserProfileResponse;
import com.pm.authservice.exception.AppException;
import com.pm.authservice.exception.ErrorCode;
import com.pm.authservice.models.RefreshToken;
import com.pm.authservice.models.User;
import com.pm.authservice.repository.RefreshTokenRepository;
import com.pm.authservice.repository.RoleRepository;
import com.pm.authservice.repository.UserRepository;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class AuthService {

    @Value("${user-service.create-path}")
    private String createUserPath;

    private final UserRepository userRepository;
    private final CustomTokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RoleRepository roleRepository;
    private final RestTemplate restTemplate;


    public AuthService(UserRepository userRepository, CustomTokenService tokenService, PasswordEncoder passwordEncoder, RefreshTokenRepository refreshTokenRepository, RoleRepository roleRepository, RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
        this.roleRepository = roleRepository;
        this.restTemplate = restTemplate;
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

    public UserProfileResponse createUser(RegisterRequest registerRequest) {
        User user = new  User();

        if (existsByEmail(registerRequest.getEmail())) {
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
        save(user);

        // lay thong tin userprofile
        UserProfileResponse userProfileResponse = new UserProfileResponse();
        userProfileResponse.setId(findByEmail(registerRequest.getEmail()).getId());
        userProfileResponse.setFullName(registerRequest.getFirstname()
                + " " + registerRequest.getLastname());
        userProfileResponse.setEmail(registerRequest.getEmail());
        userProfileResponse.setRole("USER");

        try {
            String url = createUserPath;
            restTemplate.postForObject(url, userProfileResponse, Void.class);
        } catch (Exception e) {
            userRepository.delete(user);
            throw new AppException(ErrorCode.CANT_CONNECT_USERSERVICE);
        }

        return  userProfileResponse;
    }

    public void logout(LogoutRequest request){

        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getToken()).orElseThrow(
                () -> new AppException(ErrorCode.REFRESHTOKEN_INVALID)
        );

        refreshTokenRepository.delete(refreshToken);

    }
}
