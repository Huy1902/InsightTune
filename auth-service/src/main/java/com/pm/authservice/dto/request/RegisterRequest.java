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

    private String firstname ="";

    @NotBlank(message = "NOT_BLANK")
    private String lastname;

    @Email(message = "EMAIL_INVALID", regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
    private String email;

    @Size(min = 6, message = "PASSWORD_INVALID")
    private String password;

    private String confirmPassword;
}
