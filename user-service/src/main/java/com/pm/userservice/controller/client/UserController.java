package com.pm.userservice.controller.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pm.userservice.mapper.UserMapper;
import com.pm.userservice.models.User;
import com.pm.userservice.models.dto.response.ApiResponse;
import com.pm.userservice.models.dto.request.CreateUserRequest;
import com.pm.userservice.models.dto.request.UserUpdateRequest;
import com.pm.userservice.models.dto.response.UserResponse;
import com.pm.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;


    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;

        this.userMapper = userMapper;
    }

    /*
    Create client profile.
    Long id;
    String email;
    String fullName;
    String address;
    String phone;
     */
    @PostMapping("/create")
    @Operation(summary = "Create user", description = "Receive email and password to create user")
    public ApiResponse<UserResponse> register(@Valid @RequestBody CreateUserRequest req) {
        User user = userMapper.CreateUserRequestToUser(req);

        return ApiResponse.<UserResponse>builder()
                .code(200)
                .result(userService.save(user))
                .build();

    }

    @GetMapping
    @Operation(summary = "Find user by token", description = "Find user by token")
    public ApiResponse<UserResponse> findById(Authentication authentication) {
        return ApiResponse.<UserResponse>builder()
                .code(200)
                .result(userService.findByEmail(authentication.getName()))
                .build();
    }

    /*
    Update user profile.
     */
    @PutMapping
    @Operation(summary = "Update user profile"
            , description = "Update profile user by token and UserUpdateRequest")
    public ApiResponse<UserResponse> updateUser(Authentication authentication,
                                                @Valid @RequestBody UserUpdateRequest request) throws JsonProcessingException {
        return ApiResponse.<UserResponse>builder()
                .code(200)
                .result(userService.updateUserProfile(authentication.getName(), request))
                .build();
    }

    @DeleteMapping
    @Operation(summary = "Delete user by token")
    public void deleteUser(Authentication authentication) {
        userService.deleteByEmail(authentication.getName());
    }
}
