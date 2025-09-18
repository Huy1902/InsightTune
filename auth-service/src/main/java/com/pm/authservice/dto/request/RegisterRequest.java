package com.pm.authservice.dto.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

//@RegisterChecked
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterRequest {
    @Size(min = 3, max = 15, message = "USERNAME_INVALID")
    private String firstname;

    private String lastname;

    @Email(message = "EMAIL_INVALID", regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private String email;

    @Size(min = 6, message = "PASSWORD_INVALID")
    private String password;

    private String confirmPassword;
}
