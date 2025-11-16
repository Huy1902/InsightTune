package com.pm.authservice.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LogoutRequest {

    /**
     * Refresh token của người dùng cần được thu hồi.
     */
    String token;
}
