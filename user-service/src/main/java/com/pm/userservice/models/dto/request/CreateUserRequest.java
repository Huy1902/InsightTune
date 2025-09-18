package com.pm.userservice.models.dto.request;


import lombok.*;
import lombok.experimental.FieldDefaults;

//@RegisterChecked
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateUserRequest {
    Long id;
    String email;
    String fullName;
    String address;
    String phone;
}
