package com.pm.userservice.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.pm.userservice.config.CloudinaryConfig;
import com.pm.userservice.exception.AppException;
import com.pm.userservice.exception.ErrorCode;
import com.pm.userservice.mapper.UserMapper;
import com.pm.userservice.models.User;
import com.pm.userservice.dto.request.UpdateRoleRequest;
import com.pm.userservice.dto.request.UserUpdateRequest;
import com.pm.userservice.dto.response.UserResponse;
import com.pm.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserUpdateRequest updateRequest;
    private UserResponse userResponse;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("user");
        user.setRole("USER");
        user.setAddress("123 Street");
        user.setPhone("0123456789");

        updateRequest = new UserUpdateRequest();
        updateRequest.setFirstname("New");
        updateRequest.setLastname("Name");
        updateRequest.setAddress("New Address");
        updateRequest.setPhone("0987654321");
        updateRequest.setRole("ADMIN");

        userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setEmail(user.getEmail());
        userResponse.setFirstName(user.getFirstName());
        userResponse.setLastName(user.getLastName());
        userResponse.setRole(user.getRole());
        userResponse.setAddress(user.getAddress());
        userResponse.setPhone(user.getPhone());

        userService.updateRolePath = "http://auth-service/update-role";
    }


    @Test
    void givenValidInput_whenSave_then200AndReturnJson() {
        when(userMapper.UserToUserResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.save(user);

        verify(userRepository).save(user);
        assertEquals(userResponse, result);
    }

    @Test
    void givenValidInput_whenUpdateUserProfileRoleChanged_then200AndReturnJson() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        doNothing().when(restTemplate).put(anyString(), any(UpdateRoleRequest.class));
        when(userMapper.UserToUserResponse(any())).thenReturn(userResponse);

        UserResponse result = userService.updateUserProfile("test@example.com", updateRequest);

        assertEquals(userResponse, result);
        assertEquals("ADMIN", user.getRole());
        assertEquals("New", user.getFirstName());
        assertEquals("Name", user.getLastName());
        verify(restTemplate).put(anyString(), any(UpdateRoleRequest.class));
        verify(userRepository).save(user);
    }

    @Test
    void givenValidInput_whenUpdateUserProfileRoleNotChanged_then200AndReturnJson() throws Exception {
        updateRequest.setRole("USER"); // same as current role

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userMapper.UserToUserResponse(any())).thenReturn(userResponse);

        UserResponse result = userService.updateUserProfile("test@example.com", updateRequest);

        assertEquals(userResponse, result);
        assertEquals("USER", user.getRole());
        verify(restTemplate, never()).put(anyString(), any());
        verify(userRepository).save(user);
    }

    @Test
    void givenValidInput_whenUpdateUserProfileRoleChanged_throwHttpClientErrorException() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        doThrow(HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST, "Bad Request", null, "{\"message\":\"Invalid role\"}".getBytes(), null
        )).when(restTemplate).put(anyString(), any(UpdateRoleRequest.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.updateUserProfile("test@example.com", updateRequest);
        });

        assertEquals("Invalid role", exception.getMessage());
    }

    @Test
    void givenValidInput_whenFindByEmail_then200AndReturnJson() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userMapper.UserToUserResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.findByEmail("test@example.com");

        assertEquals(userResponse, result);
    }

    @Test
    void givenInValidToken_whenFindByEmail_then400AndUserNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> {
            userService.findByEmail("unknown@example.com");
        });

        assertEquals(ErrorCode.USER_NOTFOUND, exception.getError());
    }

    @Test
    void testDeleteByEmail() {
        doNothing().when(userRepository).deleteByEmail("test@example.com");

        userService.deleteByEmail("test@example.com");

        verify(userRepository).deleteByEmail("test@example.com");
    }

    @Test
    void givenValidInput_whenChangeAvatar_then200AndUploadAndUpdateSuccessfully() throws Exception {
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar", "avatar.png", "image/png", "fake".getBytes());
        String fakeUrl = "https://fake.url/avatar.png";

        when(cloudinaryService.uploadFile(avatar)).thenReturn(fakeUrl);

        userService.changeAvatar("user@example.com", avatar);

        verify(cloudinaryService).uploadFile(avatar);
        verify(userRepository).changeAvatarByEmail("user@example.com", fakeUrl);
    }

    @Test
    void givenInvalidInputOrUploadFail_whenChangeAvatar_then400AndThrowException() throws Exception {
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar", "avatar.png", "image/png", "fake".getBytes());

        when(cloudinaryService.uploadFile(avatar)).thenThrow(new IOException("Upload failed"));

        AppException ex = assertThrows(AppException.class, () -> userService.changeAvatar("user@example.com", avatar));

        assertEquals(ErrorCode.CANT_UPLOAD_AVATAR, ex.getError());
        verify(userRepository, never()).changeAvatarByEmail(any(), any());
    }
}
