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
     * Tìm user theo email.
     *
     * @param email email người dùng
     * @return User tìm được
     * @throws AppException nếu user không tồn tại
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(()
                -> new AppException(ErrorCode.USER_NOTFOUND));
    }

    /**
     * Kiểm tra xem email đã tồn tại hay chưa.
     *
     * @param email email người dùng
     * @return true nếu email tồn tại, false nếu chưa
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Xác thực đăng nhập bằng email và password.
     *
     * @param authenticationRequest thông tin đăng nhập
     * @return AuthenticationResponse chứa access token, refresh token, trạng thái authenticated
     * @throws AppException nếu user không tồn tại hoặc mật khẩu không đúng
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

        // luu refreshToken vao db
        String refreshToken = tokenService.generateRefreshToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .authenticated(true)
                .build();
    }

    /**
     * Tạo người dùng mới và gửi thông tin sang user-service.
     *
     * @param registerRequest thông tin đăng ký
     * @return UserProfileResponse chứa thông tin người dùng vừa tạo
     * @throws AppException nếu email đã tồn tại, password không khớp hoặc không kết nối được user-service
     */
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
     * Logout người dùng, thu hồi refresh token.
     *
     * @param request thông tin logout chứa refresh token
     * @throws AppException nếu refresh token không hợp lệ
     */
    public void logout(LogoutRequest request){

        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getToken()).orElseThrow(
                () -> new AppException(ErrorCode.REFRESHTOKEN_INVALID)
        );

        refreshTokenRepository.delete(refreshToken);
    }

    /**
     * Gửi mã OTP đến địa chỉ email của người dùng để xác thực.
     * <p>
     * Phương thức này sẽ:
     * <ul>
     *   <li>Kiểm tra xem người dùng có tồn tại trong hệ thống hay không (theo email).</li>
     *   <li>Tạo mã OTP gồm 6 chữ số ngẫu nhiên.</li>
     *   <li>Lưu hoặc cập nhật mã OTP vào cơ sở dữ liệu, kèm thời gian hết hạn (5 phút).</li>
     *   <li>Gửi mã OTP đến email của người dùng thông qua {@link MailService}.</li>
     * </ul>
     * Nếu người dùng không tồn tại hoặc quá trình gửi email thất bại, phương thức sẽ ném ra ngoại lệ tương ứng.
     *
     * @param email địa chỉ email của người dùng cần gửi mã OTP.
     * @throws AppException nếu không tìm thấy người dùng tương ứng với email.
     * @throws RuntimeException nếu xảy ra lỗi khi gửi email (ví dụ: lỗi kết nối SMTP, cấu hình sai, hoặc template không hợp lệ).
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
