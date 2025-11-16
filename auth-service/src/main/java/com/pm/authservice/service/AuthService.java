package com.pm.authservice.service;

import com.pm.authservice.dto.request.LoginRequest;
import com.pm.authservice.dto.request.LogoutRequest;
import com.pm.authservice.dto.request.RegisterRequest;
import com.pm.authservice.dto.response.AuthenticationResponse;
import com.pm.authservice.dto.response.UserProfileResponse;
import com.pm.authservice.exception.AppException;
import com.pm.authservice.exception.ErrorCode;
import com.pm.authservice.models.OneTimePassword;
import com.pm.authservice.models.RefreshToken;
import com.pm.authservice.models.User;
import com.pm.authservice.repository.OneTimePasswordRepository;
import com.pm.authservice.repository.RefreshTokenRepository;
import com.pm.authservice.repository.RoleRepository;
import com.pm.authservice.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.SecureRandom;
import java.time.LocalDateTime;

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
    private final OneTimePasswordRepository oneTimePasswordRepository;
    private final MailService mailService;


    public AuthService(UserRepository userRepository, CustomTokenService tokenService,
                       PasswordEncoder passwordEncoder, RefreshTokenRepository refreshTokenRepository,
                       RoleRepository roleRepository, RestTemplate restTemplate,
                       OneTimePasswordRepository oneTimePasswordRepository, MailService mailService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
        this.roleRepository = roleRepository;
        this.restTemplate = restTemplate;
        this.oneTimePasswordRepository = oneTimePasswordRepository;
        this.mailService = mailService;
    }

    public void save(User user) {
        userRepository.save(user);
    }

    /**
     * Find a user by email.
     *
     * @param email the user's email
     * @return the found User
     * @throws AppException if the user does not exist
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(()
                -> new AppException(ErrorCode.USER_NOTFOUND));
    }

    /**
     * Check whether an email already exists.
     *
     * @param email the user's email
     * @return true if the email exists, false otherwise
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Authenticate login using email and password.
     *
     * @param authenticationRequest login information
     * @return AuthenticationResponse containing access token, refresh token, and authenticated status
     * @throws AppException if the user does not exist or the password is incorrect
     */
    public AuthenticationResponse authenticate(LoginRequest authenticationRequest) {
        // find user
        User user = userRepository.findByEmail(authenticationRequest.getEmail())
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOTFOUND));

        // authenticate password
        boolean authenticate = this.passwordEncoder.matches(
                authenticationRequest.getPassword(),
                user.getPassword()
        );
        if (!authenticate) {
            throw new AppException(ErrorCode.PASSWORD_NOT_TRUE);
        }

        // accessToken
        var token = tokenService.generateAccessToken(user);

        String refreshToken = tokenService.generateRefreshToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .authenticated(true)
                .build();
    }

    /**
     * Create a new user and send the information to the user-service.
     *
     * @param registerRequest registration information
     * @return UserProfileResponse containing the newly created user's information
     * @throws AppException if the email already exists, the password does not match, or the user-service is unreachable
     */
    public UserProfileResponse createUser(RegisterRequest registerRequest) {
        User user = new  User();

        if (existsByEmail(registerRequest.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
        }

        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(roleRepository.findByName("USER").orElseThrow(()
                -> new AppException(ErrorCode.ROLE_NOTFOUND)));
        save(user);

        UserProfileResponse userProfileResponse = UserProfileResponse.builder()
                .id(findByEmail(registerRequest.getEmail()).getId())
                .email(registerRequest.getEmail())
                .firstName(registerRequest.getFirstname())
                .lastName(registerRequest.getLastname())
                .avatar(registerRequest.getAvatar())
                .role("USER")
                .build();


        try {
            String url = createUserPath;
            restTemplate.postForObject(url, userProfileResponse, Void.class);
        } catch (Exception e) {
            userRepository.delete(user);
            throw new AppException(ErrorCode.CANT_CONNECT_USERSERVICE);
        }

        return  userProfileResponse;
    }

    /**
     * Logout a user and revoke the refresh token.
     *
     * @param request logout information containing the refresh token
     * @throws AppException if the refresh token is invalid
     */
    public void logout(LogoutRequest request){

        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getToken()).orElseThrow(
                () -> new AppException(ErrorCode.REFRESHTOKEN_INVALID)
        );

        refreshTokenRepository.delete(refreshToken);
    }

    /**
     * Sends an OTP to the user's email for verification.
     * <p>
     * This method will:
     * <ul>
     *   <li>Check if the user exists in the system (by email).</li>
     *   <li>Generate a 6-digit random OTP.</li>
     *   <li>Save or update the OTP in the database, with an expiration time of 5 minutes.</li>
     *   <li>Send the OTP to the user's email via {@link MailService}.</li>
     * </ul>
     * If the user does not exist or the email sending process fails, the method will throw the corresponding exception.
     *
     * @param email the user's email address to send the OTP to
     * @throws AppException if no user is found for the given email
     * @throws RuntimeException if an error occurs while sending the email (e.g., SMTP connection error, misconfiguration, or invalid template)
     *
     * @see MailService#sendMail(String, String)
     * @see OneTimePassword
     */
    public void sendOTP(String email) {
        if (!existsByEmail(email)) {
            throw new AppException(ErrorCode.USER_NOTFOUND);
        }

        String chars = "0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            otp.append(chars.charAt(random.nextInt(chars.length())));
        }

        OneTimePassword existing = oneTimePasswordRepository.findByEmail(email).orElse(null);

        if (existing == null) {
            OneTimePassword otp_model = OneTimePassword.builder()
                    .email(email)
                    .otp(otp.toString())
                    .expiry(LocalDateTime.now().plusMinutes(5))
                    .otp_used(false)
                    .build();

            oneTimePasswordRepository.save(otp_model);
        } else {
            existing.setOtp(otp.toString());
            existing.setExpiry(LocalDateTime.now().plusMinutes(5));
            existing.setOtp_used(false);
            oneTimePasswordRepository.save(existing);
        }

        try {
            mailService.sendMail(email, otp.toString());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}
