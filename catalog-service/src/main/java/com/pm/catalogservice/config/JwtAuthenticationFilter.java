package com.pm.catalogservice.config;

import com.nimbusds.jose.JOSEException;
import com.pm.catalogservice.service.CustomTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;

/**
 * Lọc JWT cho mỗi request (một lần) trong Spring Security.
 * <p>
 * Class này sẽ kiểm tra header "Authorization" để tìm token theo dạng "Bearer {token}".
 * Nếu token hợp lệ, nó sẽ xác thực người dùng trong SecurityContext, cho phép
 * Spring Security nhận dạng request là authenticated.
 * </p>
 * <p>
 * Các bước chính:
 * <ul>
 *     <li>Lấy token từ header Authorization</li>
 *     <li>Xác minh token bằng CustomTokenService</li>
 *     <li>Lấy email từ token và tạo UsernamePasswordAuthenticationToken</li>
 *     <li>Đặt authentication vào SecurityContextHolder</li>
 * </ul>
 * </p>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * Service để verify token và lấy thông tin từ token.
     */
    private final CustomTokenService customTokenService; // service verify token, lấy sub/email

    /**
     * Constructor để inject CustomTokenService.
     *
     * @param customTokenService service xử lý JWT
     */
    public JwtAuthenticationFilter(CustomTokenService customTokenService) {
        this.customTokenService = customTokenService;

    }

    /**
     * Lọc request HTTP để xác thực JWT.
     *
     * @param request     request HTTP
     * @param response    response HTTP
     * @param filterChain chuỗi filter tiếp theo
     * @throws ServletException nếu có lỗi Servlet
     * @throws IOException      nếu có lỗi IO
     * <p>
     * Hành vi:
     * <ul>
     *     <li>Kiểm tra header "Authorization"</li>
     *     <li>Nếu token hợp lệ, đặt authentication vào SecurityContextHolder</li>
     *     <li>Gọi filterChain.doFilter để tiếp tục xử lý request</li>
     * </ul>
     * </p>
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                if (customTokenService.verifyToken(token)) {
                    String email = customTokenService.getEmailFromToken(token);

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (ParseException | JOSEException e) {
                throw new RuntimeException(e);
            }
        }

        filterChain.doFilter(request, response);
    }
}
