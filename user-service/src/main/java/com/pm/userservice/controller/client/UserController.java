package com.pm.userservice.controller.client;

import com.pm.userservice.mapper.UserMapper;
import com.pm.userservice.models.User;
import com.pm.userservice.models.dto.request.ApiResponse;
import com.pm.userservice.models.dto.request.CreateUserRequest;
import com.pm.userservice.models.dto.request.UserUpdateRequest;
import com.pm.userservice.models.dto.response.UserResponse;
import com.pm.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    public ApiResponse<UserResponse> register(@Valid @RequestBody CreateUserRequest req) {
        User user = userMapper.CreateUserRequestToUser(req);

        return ApiResponse.<UserResponse>builder()
                .code(200)
                .result(userService.save(user))
                .build();

    }

    @GetMapping
    public ApiResponse<List<UserResponse>> findAll() {
        return ApiResponse.<List<UserResponse>>builder()
                .code(200)
                .result(userService.findAll())
                .build();
    }


    @GetMapping("/{userId}")
    public ApiResponse<UserResponse> findById(@PathVariable Long userId) {
        return ApiResponse.<UserResponse>builder()
                .code(200)
                .result(userService.findById(userId))
                .build();
    }

    /*
    Update user profile.
     */
    @PutMapping("/{userId}")
    public ApiResponse<UserResponse> updateUser(@PathVariable Long userId,
                                           @RequestBody UserUpdateRequest request) {
        return ApiResponse.<UserResponse>builder()
                .code(200)
                .result(userService.updateUserProfile(userId, request))
                .build();
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Long userId) {
        userService.deleteById(userId);
    }
}
