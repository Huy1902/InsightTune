package com.pm.playingservice.filter;

import com.pm.playingservice.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;

  public JwtRequestFilter(JwtUtil jwtUtil) {
    this.jwtUtil = jwtUtil;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain chain)
          throws ServletException, IOException {

    String auth = request.getHeader("Authorization");
    if (auth != null && auth.startsWith("Bearer ")) {
      String jwt = auth.substring(7);
      try {
        Claims claims = jwtUtil.parseClaims(jwt);

        String email = claims.getSubject();
        if (email != null && !email.isBlank()) {
          log.info("Receive token contain email: {}", email);
          UsernamePasswordAuthenticationToken authentication =
                  new UsernamePasswordAuthenticationToken(email, null, List.of()); // no roles
          authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
          log.warn("JWT subject (email) is missing or blank");
          SecurityContextHolder.clearContext();
        }

      } catch (JwtException | IllegalArgumentException e) {
        log.warn("JWT rejected: {}", e.getMessage());
        SecurityContextHolder.clearContext();
      }
    }
    chain.doFilter(request, response);
  }
}
