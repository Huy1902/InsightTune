package com.pm.playingservice.config;

import com.pm.playingservice.filter.JwtRequestFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http, JwtRequestFilter jwt) throws Exception {
    return http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))   // ✅ no sessions/cookies
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.GET, "/user_state").authenticated()
                    .requestMatchers(HttpMethod.POST, "/user_state").authenticated()
                    .requestMatchers(HttpMethod.POST, "/play")
                    .hasAnyRole("USER", "ADMIN")
                    .anyRequest().permitAll())
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint((req, res, e) ->
                            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")))      // ✅ 401 on missing/invalid JWT
            .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class)
            .build();
  }
}
