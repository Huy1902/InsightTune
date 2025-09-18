package com.pm.userservice.mapper;

import com.pm.userservice.models.User;
import com.pm.userservice.models.dto.request.CreateUserRequest;
import com.pm.userservice.models.dto.response.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User CreateUserRequestToUser(CreateUserRequest req) {
        User user = new  User();

        user.setId(req.getId());
        user.setEmail(req.getEmail());
        user.setFullName(req.getFullName());
        user.setAddress(req.getAddress());
        user.setPhone(req.getPhone());

        return user;
    }

    public UserResponse UserToUserResponse(User user) {
        UserResponse userResponse = new UserResponse();

        userResponse.setId(user.getId());
        userResponse.setEmail(user.getEmail());
        userResponse.setFullName(user.getFullName());
        userResponse.setAddress(user.getAddress());
        userResponse.setPhone(user.getPhone());

        return userResponse;
    }
}
