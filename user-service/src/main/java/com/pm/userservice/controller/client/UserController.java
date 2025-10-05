package com.pm.userservice.controller.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pm.userservice.dto.request.UpdateAvatarRequest;
import com.pm.userservice.mapper.UserMapper;
import com.pm.userservice.models.User;
import com.pm.userservice.dto.response.ApiResponse;
import com.pm.userservice.dto.request.CreateUserRequest;
import com.pm.userservice.dto.request.UserUpdateRequest;
import com.pm.userservice.dto.response.UserResponse;
import com.pm.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

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
    public ApiResponse<UserResponse> findUser(Principal principal) {
        return ApiResponse.<UserResponse>builder()
                .code(200)
                .result(userService.findByEmail(principal.getName()))
                .build();
    }

    /*
    Update user profile.
     */
    @PutMapping
    @Operation(summary = "Update user profile"
            , description = "Update profile user by token and UserUpdateRequest")
    public ApiResponse<UserResponse> updateUser(Principal principal,
                                                @Valid @RequestBody UserUpdateRequest request) throws JsonProcessingException {
        return ApiResponse.<UserResponse>builder()
                .code(200)
                .result(userService.updateUserProfile(principal.getName(), request))
                .build();
    }

    @DeleteMapping
    @Operation(summary = "Delete user by token")
    public void deleteUser(Principal principal) {
        userService.deleteByEmail(principal.getName());
    }

    @PutMapping("/avatar")
    @Operation(summary = "Change user avatar")
    public ApiResponse<String> changeAvatar(Principal principal, @RequestPart("avatar") MultipartFile avatar) {
        return ApiResponse.<String>builder()
                .code(200)
                .message("Change user avatar successfully")
                .result(userService.changeAvatar(principal.getName(), avatar))
                .build();
    }
}
