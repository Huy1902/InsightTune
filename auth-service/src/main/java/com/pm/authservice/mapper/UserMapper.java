package com.pm.authservice.mapper;

import com.pm.authservice.models.User;
import com.pm.authservice.dto.request.RegisterRequest;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User RegisterRequestToUser(RegisterRequest req) {
        User user = new  User();

        user.setEmail(req.getEmail());
        user.setPassword(req.getPassword());

        return user;
    }

}
