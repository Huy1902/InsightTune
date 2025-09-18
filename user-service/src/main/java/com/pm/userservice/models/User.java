package com.pm.userservice.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;


    @Email(message = "EMAIL_INVALID")
    @NotBlank
    String email;

    @NotBlank
    @Size(min = 6, message = "Password phải có tối thiểu 6 ký tự")
    String password;

    @NotBlank
    @Size(min = 3, max = 15, message = "FullName phải có tối thiểu 3 ký tự")
    String fullName;

    String address;

    String phone;

    String avatar;

    // roleid
    @ManyToOne
    @JoinColumn(name = "role_id")
    Role role;
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                ", avatar='" + avatar + '\'' +
                '}';
    }


}
