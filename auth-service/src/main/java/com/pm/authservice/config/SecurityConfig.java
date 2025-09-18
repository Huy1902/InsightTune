package com.pm.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // tạm thời tắt CSRF cho API
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/**", "/register").permitAll() // cho phép public
                        .anyRequest().authenticated() // các endpoint khác cần login
                );

        return http.build();
    }
}
