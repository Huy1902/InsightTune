package com.pm.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.userservice.controller.client.UserController;
import com.pm.userservice.mapper.UserMapper;
import com.pm.userservice.models.User;
import com.pm.userservice.dto.request.CreateUserRequest;
import com.pm.userservice.dto.request.UserUpdateRequest;
import com.pm.userservice.dto.response.UserResponse;
import com.pm.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false) // keep security filters (if any) out of the slice
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserMapper userMapper;

    private User user;
    private UserResponse userResponse;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setFullName("Test User");

        userResponse = new UserResponse();
        userResponse.setId(1L);
        userResponse.setEmail("test@example.com");
        userResponse.setFullName("Test User");
    }

    @Test
    void testRegister_success() throws Exception {
        CreateUserRequest req = new CreateUserRequest();
        req.setEmail("test@example.com");

        when(userMapper.CreateUserRequestToUser(any(CreateUserRequest.class)))
                .thenReturn(user);
        when(userService.save(any(User.class))).thenReturn(userResponse);

        mockMvc.perform(post("/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.result.email").value("test@example.com"));
    }

    @Test
    void testRegister_fail() throws Exception {
        CreateUserRequest req = new CreateUserRequest();
        req.setEmail("fail@example.com");

        when(userMapper.CreateUserRequestToUser(any(CreateUserRequest.class)))
                .thenReturn(null);

        // service sẽ null → mock ném lỗi
        when(userService.save(null)).thenThrow(new RuntimeException("Save failed"));

        mockMvc.perform(post("/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testFindUser_success() throws Exception {
        when(userService.findByEmail("test@example.com")).thenReturn(userResponse);

        mockMvc.perform(get("/users")
                        .principal(() -> "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.email").value("test@example.com"))
                .andExpect(jsonPath("$.result.fullName").value("Test User"));
    }

    @Test
    void testUpdateUser_success() throws Exception {
        UserUpdateRequest req = new UserUpdateRequest();
        req.setFirstname("John");
        req.setLastname("Doe");
        req.setAddress("123 Street");
        req.setPhone("123456789");
        req.setRole("USER");

        when(userService.updateUserProfile(any(), any()))
                .thenReturn(userResponse);

        mockMvc.perform(put("/users")
                        .principal(() -> "test@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.email").value("test@example.com"));
    }

    @Test
    void testDeleteUser_success() throws Exception {
        mockMvc.perform(delete("/users")
                        .principal(() -> "test@example.com"))
                .andExpect(status().isOk());
    }
}
