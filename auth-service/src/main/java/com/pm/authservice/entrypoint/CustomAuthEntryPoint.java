package com.pm.authservice.entrypoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.authservice.dto.response.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Xử lý các trường hợp người dùng chưa xác thực (unauthenticated) khi truy cập API bảo vệ.
 * <p>
 * Khi Spring Security phát hiện request không hợp lệ hoặc không có token,
 * class này sẽ trả về HTTP status 401 Unauthorized kèm theo JSON thông báo lỗi.
 * </p>
 */
@Component
public class CustomAuthEntryPoint implements AuthenticationEntryPoint {

    /**
     * Gọi khi authentication thất bại.
     *
     * @param request       request HTTP
     * @param response      response HTTP
     * @param authException ngoại lệ xác thực
     * @throws IOException      nếu ghi JSON vào response lỗi
     * @throws ServletException nếu có lỗi Servlet
     * <p>
     * Hành vi:
     * <ul>
     *     <li>Set content type là application/json</li>
     *     <li>Set HTTP status là 401 Unauthorized</li>
     *     <li>Trả về body JSON: {"code":401, "message":"Lỗi Token"}</li>
     * </ul>
     * </p>
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ApiResponse<Object> apiResponse = ApiResponse.builder()
                .code(401)
                .message("Lỗi Token")
                .build();

        // Chuyển ApiResponse thành JSON
        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(apiResponse));
    }
}
