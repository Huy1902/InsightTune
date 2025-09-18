package com.pm.userservice.controller.client;

import com.pm.userservice.exception.AppException;
import com.pm.userservice.exception.ErrorCode;
import com.pm.userservice.mapper.UserMapper;
import com.pm.userservice.models.User;
import com.pm.userservice.models.dto.request.ApiResponse;
import com.pm.userservice.models.dto.request.RegisterDTO;
import com.pm.userservice.models.dto.request.UserUpdateRequest;
import com.pm.userservice.models.dto.response.UserResponse;
import com.pm.userservice.service.RoleService;
import com.pm.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;


    public UserController(UserService userService, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@Valid @RequestBody RegisterDTO userDto) {

        if (userService.existsByEmail(userDto.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        if (!userDto.getPassword().equals(userDto.getConfirmPassword())) {
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
        }

        User user = userMapper.registerDtoToUser(userDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

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

    @PutMapping("/{userId}")
    public ApiResponse<UserResponse> updateUser(@PathVariable Long userId,
                                           @RequestBody UserUpdateRequest request) {
        return ApiResponse.<UserResponse>builder()
                .code(200)
                .result(userService.updateUser(userId, request))
                .build();
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Long userId) {
        userService.deleteById(userId);
    }
}
