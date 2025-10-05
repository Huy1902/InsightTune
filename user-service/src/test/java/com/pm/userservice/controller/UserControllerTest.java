package com.pm.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.userservice.config.CloudinaryConfig;
import com.pm.userservice.controller.client.UserController;
import com.pm.userservice.dto.request.UpdateAvatarRequest;
import com.pm.userservice.mapper.UserMapper;
import com.pm.userservice.models.User;
import com.pm.userservice.dto.request.CreateUserRequest;
import com.pm.userservice.dto.request.UserUpdateRequest;
import com.pm.userservice.dto.response.UserResponse;
import com.pm.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

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
        user.setFirstName("Test");
        user.setLastName("user");

        userResponse = new UserResponse();
        userResponse.setId(1L);
        userResponse.setEmail("test@example.com");
        userResponse.setFirstName("Test");
        userResponse.setLastName("user");
    }

    @Test
    void givenValidInput_whenRegister_then200andReturnJson() throws Exception {
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
    void givenInValidInput_whenRegister_then400andThrowException() throws Exception {
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
    void givenValidToken_whenFindUser_then200andReturnJson() throws Exception {
        when(userService.findByEmail("test@example.com")).thenReturn(userResponse);

        mockMvc.perform(get("/users")
                        .principal(() -> "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.email").value("test@example.com"))
                .andExpect(jsonPath("$.result.firstName").value("Test"))
                .andExpect(jsonPath("$.result.lastName").value("user"));

    }

    @Test
    void givenValidInput_whenUpdateUserProfile_then200andReturnJson() throws Exception {
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
    void givenValidToken_whenDelete_then200() throws Exception {
        mockMvc.perform(delete("/users")
                        .principal(() -> "test@example.com"))
                .andExpect(status().isOk());
    }

    @Test
    void testChangeAvatar() throws Exception {
        // Tạo file giả lập
        MockMultipartFile file = new MockMultipartFile(
                "avatar",
                "avatar.png",
                "image/png",
                "fake-image-content".getBytes()
        );

        // Mock service trả về URL
        when(userService.changeAvatar("testUser", file)).thenReturn("avatar-url.png");

        mockMvc.perform(multipart("/users/avatar")
                        .file(file)
                        .principal(() -> "testUser") // gán Principal trực tiếp
                        .with(request -> {
                            request.setMethod("PUT"); // chuyển POST -> PUT
                            return request;
                        })
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Change user avatar successfully"))
                .andExpect(jsonPath("$.result").value("avatar-url.png")); // bây giờ sẽ có result
    }
}