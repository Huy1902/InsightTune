package com.pm.userservice.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Builder
@Entity
@Table(name = "users")
public class User {


    @Id
    Long id;

    @Email(message = "EMAIL_INVALID")
    @NotBlank
    String email;

    @NotBlank(message = "NOT_BLANK")
    String firstName;

    @NotBlank(message = "NOT_BLANK")
    String lastName;

    String address;

    String phone;

    String role;

    String avatar;
}
