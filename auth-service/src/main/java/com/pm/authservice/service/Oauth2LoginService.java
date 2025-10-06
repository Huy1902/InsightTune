package com.pm.authservice.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.pm.authservice.dto.request.LoginRequest;
import com.pm.authservice.dto.request.Oauth2LoginRequest;
import com.pm.authservice.dto.request.RegisterRequest;
import com.pm.authservice.dto.response.AuthenticationResponse;
import com.pm.authservice.dto.response.UserProfileResponse;
import com.pm.authservice.exception.AppException;
import com.pm.authservice.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

/**
 * Service xử lý login OAuth2 với Google.
 * <p>
 * Bao gồm các chức năng:
 * <ul>
 *     <li>Verify Google ID Token</li>
 *     <li>Login bằng Google</li>
 *     <li>Tự động tạo user mới nếu email chưa tồn tại</li>
 * </ul>
 * </p>
 */
@Service
@Slf4j
public class Oauth2LoginService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    String client_id;

    private final AuthService authService;

    public Oauth2LoginService(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Verify Google ID Token từ client.
     *
     * @param idTokenString chuỗi ID token từ Google
     * @return GoogleIdToken đã verify hợp lệ, hoặc null nếu không hợp lệ
     * @throws GeneralSecurityException nếu xác thực bảo mật thất bại
     * @throws IOException              nếu có lỗi I/O
     */
    protected GoogleIdToken verifyGoogleIdToken(String idTokenString) throws GeneralSecurityException, IOException {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                JacksonFactory.getDefaultInstance()
        )
                .setAudience(Collections.singletonList(client_id))
                .build();

        return verifier.verify(idTokenString);
    }

    /**
     * Login bằng Google OAuth2.
     * <p>
     * Nếu user chưa tồn tại, sẽ tự động tạo user mới với password mặc định "GOOGLE_LOGIN".
     * Sau đó tiến hành authenticate và trả về access token + refresh token.
     * </p>
     *
     * @param request Oauth2LoginRequest chứa Google ID token
     * @return AuthenticationResponse chứa access token, refresh token, trạng thái authenticated
     * @throws GeneralSecurityException nếu xác thực token thất bại
     * @throws IOException              nếu có lỗi I/O
     * @throws AppException             nếu ID token null hoặc không hợp lệ
     */
    public AuthenticationResponse loginGoogle(Oauth2LoginRequest request) throws GeneralSecurityException, IOException {
        String idTokenString = request.getIdToken();

        GoogleIdToken idToken = verifyGoogleIdToken(idTokenString);
        if (idToken == null) {
            throw new AppException(ErrorCode.IDTOKEN_NULL);
        }

        GoogleIdToken.Payload payload = idToken.getPayload();

        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String picture = (String) payload.get("picture");

        boolean checkUser = authService.existsByEmail(email);
        if (!checkUser) {
            // Tách chuỗi thành mảng từ, bỏ bớt khoảng trắng thừa
            String[] parts = (name != null ? name.trim().split("\\s+") : new String[0]);
            String firstName = parts.length > 0 ? parts[0] : "Google";
            String lastName = parts.length > 1 ? parts[parts.length - 1] : "User";

            RegisterRequest registerRequest = RegisterRequest.builder()
                    .email(email)
                    .firstname(firstName)
                    .lastname(lastName)
                    .password("GOOGLE_LOGIN")
                    .confirmPassword("GOOGLE_LOGIN")
                    .avatar(picture)
                    .build();

            UserProfileResponse userProfileResponse = authService.createUser(registerRequest);
            log.info("userProfileResponse={}", userProfileResponse);
        }

        LoginRequest loginRequest = LoginRequest.builder()
                .email(email)
                .password("GOOGLE_LOGIN")
                .build();

        return authService.authenticate(loginRequest);
    }
}
