package com.pm.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    @NotBlank(message = "NOT_BLANK")
    String firstname;

    @NotBlank(message = "NOT_BLANK")
    String lastname;

    @NotBlank(message = "NOT_BLANK")
    String address;

    @NotBlank(message = "NOT_BLANK")
    String phone;

    @NotBlank(message = "NOT_BLANK")
    String role;
}
