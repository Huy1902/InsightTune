package com.pm.userservice.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Getter
@Setter
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

    @NotBlank
    @Size(min = 3, max = 15, message = "FullName phải có tối thiểu 3 ký tự")
    String fullName;

    String address;

    String phone;

    String role;

    String avatar;
}
