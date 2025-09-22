package com.pm.authservice.config;

import com.pm.authservice.config.oauth2.handlers.CustomOAuth2SuccessHandler;
import com.pm.authservice.entrypoint.CustomAuthEntryPoint;
import com.pm.authservice.service.CustomOAuth2UserService;
import com.pm.authservice.service.CustomTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.crypto.spec.SecretKeySpec;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
@ComponentScan(basePackages = "com.pm.authservice")
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomAuthEntryPoint customAuthEntryPoint;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOAuth2SuccessHandler  customOAuth2SuccessHandler;


    private final String[] PUBLIC_ENDPOINTS = {"/auth/login", "/auth/register"};

    @Value("${jwt.signerKey}")
    private String SIGNER_KEY;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, CustomAuthEntryPoint customAuthEntryPoint, CustomOAuth2UserService customOAuth2UserService, CustomOAuth2SuccessHandler customOAuth2SuccessHandler) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.customAuthEntryPoint = customAuthEntryPoint;
        this.customOAuth2UserService = customOAuth2UserService;
        this.customOAuth2SuccessHandler = customOAuth2SuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // tạm thời tắt CSRF cho API
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS).permitAll() // cho phép public
                        .anyRequest().authenticated()
                        // các endpoint khác cần login
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        http.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                        .decoder(jwtDecoder())
                        .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                .authenticationEntryPoint(customAuthEntryPoint) // <-- dùng custom 401
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                        .userService(customOAuth2UserService)
                                // inject service bạn viết
                        )
                        .successHandler(customOAuth2SuccessHandler)
                );
        return http.build();
    }

    @Bean
    JwtDecoder jwtDecoder() {
        SecretKeySpec secretKeySpec = new SecretKeySpec(SIGNER_KEY.getBytes(), "HS512");
        return NimbusJwtDecoder
                .withSecretKey(secretKeySpec)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
    };

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

        return jwtAuthenticationConverter;
    }
}
