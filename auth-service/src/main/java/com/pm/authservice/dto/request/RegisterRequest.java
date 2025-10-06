package com.pm.authservice.dto.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterRequest {

    @NotBlank(message = "NOT_BLANK")
    String firstname ;

    @NotBlank(message = "NOT_BLANK")
    String lastname;

    @Email(message = "EMAIL_INVALID", regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
    String email;

    @Size(min = 6, message = "PASSWORD_INVALID")
    String password;

    String confirmPassword;

    String avatar;
}
