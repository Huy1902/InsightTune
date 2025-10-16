package com.pm.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ForgotPasswordRequest {
    @NotBlank(message = "NOT_BLANK")
    String otp;

    @NotBlank(message = "NOT_BLANK")
    String email;

    @Size(min = 6, message = "PASSWORD_INVALID")
    String newPassword;

    @NotBlank(message = "NOT_BLANK")
    String confirmNewPassword;
}
