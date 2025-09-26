package com.pm.userservice.models.dto.request;

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

    String address;
    String phone;

    @NotBlank(message = "NOT_BLANK")
    String role;
}
