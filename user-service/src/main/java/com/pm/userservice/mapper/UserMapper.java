package com.pm.userservice.mapper;

import com.pm.userservice.models.User;
import com.pm.userservice.dto.request.CreateUserRequest;
import com.pm.userservice.dto.response.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User CreateUserRequestToUser(CreateUserRequest req) {
        return User.builder()
                .id(req.getId())
                .email(req.getEmail())
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .address(req.getAddress())
                .phone(req.getPhone())
                .role(req.getRole())
                .build();
    }

    public UserResponse UserToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .address(user.getAddress())
                .phone(user.getPhone())
                .role(user.getRole())
                .avatar(user.getAvatar())
                .build();
    }
}
