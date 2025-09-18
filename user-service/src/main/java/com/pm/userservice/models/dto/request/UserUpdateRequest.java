package com.pm.userservice.models.dto.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserUpdateRequest {
    private String firstname;
    private String lastname;
    private String password;
    private String address;
    private String phone;

}
