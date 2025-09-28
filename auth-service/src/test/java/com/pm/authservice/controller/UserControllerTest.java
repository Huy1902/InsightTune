package com.pm.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.authservice.config.JwtAuthenticationFilter;
import com.pm.authservice.dto.request.UpdateRoleRequest;
import com.pm.authservice.dto.response.ApiResponse;
import com.pm.authservice.repository.RefreshTokenRepository;
import com.pm.authservice.repository.RoleRepository;
import com.pm.authservice.repository.UserRepository;
import com.pm.authservice.service.CustomTokenService;
import com.pm.authservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false) // keep security filters (if any) out of the slice
public class UserControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockitoBean
    private RoleRepository roleRepository;
    @MockitoBean
    private CustomTokenService customTokenService;
    @MockitoBean
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    void givenValidInput_whenUpdateRole_then200AndReturnsDtoJson() throws Exception {
        UpdateRoleRequest updateRoleRequest =  UpdateRoleRequest.builder()
                .email("test@example.com")
                .role("ADMIN")
                .build();


        ApiResponse<String> mockResponse = ApiResponse.<String>builder()
                        .code(200)
                        .build();

        when(userService.updateRole(any(UpdateRoleRequest.class))).thenReturn(mockResponse.getCode());

        mockMvc.perform(put("/user/updateRole")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateRoleRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

    }
}
