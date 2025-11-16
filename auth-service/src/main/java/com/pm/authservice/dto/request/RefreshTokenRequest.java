package com.pm.authservice.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * DTO dùng để gửi thông tin refresh token từ client.
 * <p>
 * Client gửi token này khi muốn lấy lại access token mới.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RefreshTokenRequest {

    /**
     * Refresh token hiện tại của người dùng.
     * <p>
     * Server sẽ xác thực token này và trả về access token mới nếu hợp lệ.
     * </p>
     */
    String token;
}
