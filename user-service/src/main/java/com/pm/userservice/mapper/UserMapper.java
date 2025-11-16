package com.pm.userservice.mapper;

import com.pm.userservice.models.User;
import com.pm.userservice.dto.request.CreateUserRequest;
import com.pm.userservice.dto.response.UserResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    @Value("${avatar.url}")
    String avatarURL;

    public User CreateUserRequestToUser(CreateUserRequest req) {
        if (req.getAvatar() == null || req.getAvatar().isEmpty()) {
            req.setAvatar(avatarURL);
        }
        return User.builder()
                .id(req.getId())
                .email(req.getEmail())
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .address(req.getAddress())
                .phone(req.getPhone())
                .avatar(req.getAvatar())
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
