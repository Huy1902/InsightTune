package com.pm.userservice.mapper;

import com.pm.userservice.models.User;
import com.pm.userservice.models.dto.request.RegisterDTO;
import com.pm.userservice.models.dto.response.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User registerDtoToUser(RegisterDTO userDto) {
        User user = new User();

        user.setEmail(userDto.getEmail());
        user.setPassword(userDto.getPassword());
        user.setFullName(userDto.getFirstname() + " " + userDto.getLastname());

        return user;
    }

    public UserResponse UserToUserResponse(User user) {
        UserResponse userResponse = new UserResponse();

        userResponse.setId(user.getId());
        userResponse.setUsername(user.getEmail());
        userResponse.setFullName(user.getFullName());
        userResponse.setRole(user.getRole().getName());

        return userResponse;
    }
}
