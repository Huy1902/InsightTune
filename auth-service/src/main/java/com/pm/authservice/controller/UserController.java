package com.pm.authservice.controller;

import com.pm.authservice.dto.request.ChangePasswordRequest;
import com.pm.authservice.dto.request.ForgotPasswordRequest;
import com.pm.authservice.dto.request.UpdateRoleRequest;
import com.pm.authservice.dto.response.ApiResponse;
import com.pm.authservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * Controller managing APIs related to users.
 * <p>
 * Includes the following functionalities:
 * <ul>
 *     <li>Update user role</li>
 *     <li>Change user password</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * API to update a user's role.
     * <p>
     * This API is usually called by other services; the frontend does not need to call it directly.
     * </p>
     *
     * @param updateRoleRequest information for updating the role (email, new role)
     * @return ApiResponse confirming the successful update
     */
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

    /**
     * API to change a user's password.
     * <p>
     * A valid access token is required to authenticate the user. The user provides the old password and the new password.
     * </p>
     *
     * @param updatePassword information for changing the password (oldPassword, newPassword)
     * @param principal      the current user's principal (retrieves username/email from the token)
     * @return ApiResponse confirming the successful password change
     */
    @PutMapping("/changePassword")
    @Operation(summary = "Change User password", description = "Change user password, need accessToken")
    public ApiResponse<String> changePassword(@Valid @RequestBody ChangePasswordRequest updatePassword, Principal principal) {
        userService.changePassword(updatePassword, principal.getName());
        return ApiResponse.<String>builder()
                .code(200)
                .result("Change password successful")
                .build();
    }

    /**
     * API to reset password when the user forgets it.
     *
     * @param forgotPasswordRequest contains email, OTP, new password, and confirmNewPassword
     * @return ApiResponse confirming successful password reset
     */
    @PatchMapping("/forgotPassword")
    @Operation(summary = "Change User password", description = "Not need accessToken")
    public ApiResponse<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        userService.forgotPassword(forgotPasswordRequest);
        return ApiResponse.<String>builder()
                .code(200)
                .result("Change password successful")
                .build();
    }
}
