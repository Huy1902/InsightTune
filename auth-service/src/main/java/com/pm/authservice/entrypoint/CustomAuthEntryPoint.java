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
 * Handles cases where a user is unauthenticated when accessing protected APIs.
 * <p>
 * When Spring Security detects an invalid request or a missing token,
 * this class returns HTTP status 401 Unauthorized along with a JSON error message.
 * </p>
 */
@Component
public class CustomAuthEntryPoint implements AuthenticationEntryPoint {

    /**
     * Called when authentication fails.
     *
     * @param request       the HTTP request
     * @param response      the HTTP response
     * @param authException the authentication exception
     * @throws IOException      if writing JSON to the response fails
     * @throws ServletException if a Servlet error occurs
     * <p>
     * Behavior:
     * <ul>
     *     <li>Set content type to application/json</li>
     *     <li>Set HTTP status to 401 Unauthorized</li>
     *     <li>Return JSON body: {"code":401, "message":"Token Error"}</li>
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

        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(apiResponse));
    }
}
