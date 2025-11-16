package com.pm.userservice.dto.request;


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
    String firstName;
    String lastName;
    String address;
    String phone;
    String role;
    String avatar;
}
