package com.pm.authservice.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * DTO dùng để gửi thông tin đăng nhập OAuth2 từ client.
 * <p>
 * Hiện tại sử dụng cho Google login, chứa ID token nhận được từ Google.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Oauth2LoginRequest {

    /**
     * ID token của người dùng do Google cấp.
     * <p>
     * Token này sẽ được server xác thực để đăng nhập người dùng.
     * </p>
     */
    String idToken;
}
