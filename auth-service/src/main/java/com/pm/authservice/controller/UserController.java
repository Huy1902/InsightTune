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
 * Controller quản lý các API liên quan đến người dùng.
 * <p>
 * Bao gồm các chức năng:
 * <ul>
 *     <li>Cập nhật vai trò người dùng (update role)</li>
 *     <li>Đổi mật khẩu người dùng (change password)</li>
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
     * API cập nhật vai trò của người dùng.
     * <p>
     * API này thường được gọi từ các service khác, frontend không cần gọi trực tiếp.
     * </p>
     *
     * @param updateRoleRequest thông tin update role (email, role mới)
     * @return ApiResponse xác nhận update thành công
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
     * API đổi mật khẩu người dùng.
     * <p>
     * Cần access token hợp lệ để xác thực user. User gửi password cũ và password mới.
     * </p>
     *
     * @param updatePassword thông tin đổi mật khẩu (oldPassword, newPassword)
     * @param principal      principal của user hiện tại (lấy username/email từ token)
     * @return ApiResponse xác nhận đổi mật khẩu thành công
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
     * API đổi mật khẩu khi người dùng quên mật khẩu.
     *
     * @param forgotPasswordRequest thông tin email, OTP và mật khẩu mới, confirmNewPassword
     * @return ApiResponse xác nhận đổi mật khẩu thành công
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
