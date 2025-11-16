package com.pm.authservice.config;

import com.nimbusds.jose.JOSEException;
import com.pm.authservice.service.CustomTokenService;
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
 * Filters JWT for each request (once per request) in Spring Security.
 * <p>
 * This class checks the "Authorization" header for a token in the format "Bearer {token}".
 * If the token is valid, it authenticates the user in the SecurityContext,
 * allowing Spring Security to recognize the request as authenticated.
 * </p>
 * <p>
 * Main steps:
 * <ul>
 *     <li>Extract the token from the Authorization header</li>
 *     <li>Verify the token using CustomTokenService</li>
 *     <li>Retrieve the email from the token and create a UsernamePasswordAuthenticationToken</li>
 *     <li>Set the authentication into the SecurityContextHolder</li>
 * </ul>
 * </p>
 */

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * Service to verify tokens and extract information from them.
     */
    private final CustomTokenService customTokenService; // service verify token, lấy sub/email

    /**
     * Constructor to inject CustomTokenService.
     *
     * @param customTokenService service that handles JWT
     */
    public JwtAuthenticationFilter(CustomTokenService customTokenService) {
        this.customTokenService = customTokenService;

    }

    /**
     * Filters the HTTP request to authenticate the JWT.
     *
     * @param request     the HTTP request
     * @param response    the HTTP response
     * @param filterChain the next filter in the chain
     * @throws ServletException if a Servlet error occurs
     * @throws IOException      if an I/O error occurs
     * <p>
     * Behavior:
     * <ul>
     *     <li>Check the "Authorization" header</li>
     *     <li>If the token is valid, set the authentication into the SecurityContextHolder</li>
     *     <li>Call filterChain.doFilter to continue processing the request</li>
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
