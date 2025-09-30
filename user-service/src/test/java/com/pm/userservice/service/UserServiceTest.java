package com.pm.userservice.service;

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
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

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
        user.setFullName("Test User");
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
        userResponse.setFullName(user.getFullName());
        userResponse.setRole(user.getRole());
        userResponse.setAddress(user.getAddress());
        userResponse.setPhone(user.getPhone());

        userService.updateRolePath = "http://auth-service/update-role";
    }

    @Test
    void testSave_success() {
        when(userMapper.UserToUserResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.save(user);

        verify(userRepository).save(user);
        assertEquals(userResponse, result);
    }

    @Test
    void testUpdateUserProfile_roleChanged_success() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        doNothing().when(restTemplate).put(anyString(), any(UpdateRoleRequest.class));
        when(userMapper.UserToUserResponse(any())).thenReturn(userResponse);

        UserResponse result = userService.updateUserProfile("test@example.com", updateRequest);

        assertEquals(userResponse, result);
        assertEquals("ADMIN", user.getRole());
        assertEquals("New Name", user.getFullName());
        verify(restTemplate).put(anyString(), any(UpdateRoleRequest.class));
        verify(userRepository).save(user);
    }

    @Test
    void testUpdateUserProfile_roleNotChanged_success() throws Exception {
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
    void testUpdateUserProfile_httpClientErrorException() throws Exception {
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
    void testFindByEmail_success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userMapper.UserToUserResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.findByEmail("test@example.com");

        assertEquals(userResponse, result);
    }

    @Test
    void testFindByEmail_userNotFound() {
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
}
