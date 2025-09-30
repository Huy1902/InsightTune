package com.pm.authservice.controller;

import com.pm.authservice.dto.request.ChangePasswordRequest;
import com.pm.authservice.dto.request.UpdateRoleRequest;
import com.pm.authservice.dto.response.ApiResponse;
import com.pm.authservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/updateRole")
    @Operation(summary = "Update Role", description = "Be call this api through update user profile in user-service" +
            ", fe don't have to call this api")
    public ApiResponse<String> updateRole(@RequestBody UpdateRoleRequest updateRoleRequest) {
        userService.updateRole(updateRoleRequest);
        return ApiResponse.<String>builder()
                .code(200)
                .result("Update role successful")
                .build();
    }

    @PutMapping("/changePassword")
    @Operation(summary = "Chang User password", description = "Change user password, need accessToken")
    public ApiResponse<String> changePassword(@Valid @RequestBody ChangePasswordRequest updatePassword, Principal principal) {
        userService.changePassword(updatePassword, principal.getName());
        return ApiResponse.<String>builder()
                .code(200)
                .result("Change password successful")
                .build();
    }
}
