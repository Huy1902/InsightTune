package com.pm.userservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.userservice.exception.AppException;
import com.pm.userservice.exception.ErrorCode;
import com.pm.userservice.mapper.UserMapper;
import com.pm.userservice.models.User;
import com.pm.userservice.models.dto.request.UpdateRoleRequest;
import com.pm.userservice.models.dto.request.UserUpdateRequest;
import com.pm.userservice.models.dto.response.UserResponse;
import com.pm.userservice.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class UserService {
    @Value("${auth-service.update-role}")
    private String updateRolePath;
    private final RestTemplate restTemplate;


    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(RestTemplate restTemplate, UserRepository userRepository, UserMapper userMapper) {
        this.restTemplate = restTemplate;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public UserResponse save(User user) {
        userRepository.save(user);
        return userMapper.UserToUserResponse(user);
    }

    @PostAuthorize("returnObject.email == authentication.name")
    public UserResponse updateUserProfile(String email, UserUpdateRequest request) throws JsonProcessingException {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOTFOUND));

        user.setFullName(request.getFirstname() + " " + request.getLastname());
        user.setAddress(request.getAddress());
        user.setPhone(request.getPhone());

        UpdateRoleRequest updateRoleRequest = new UpdateRoleRequest();
        updateRoleRequest.setRole(request.getRole());
        updateRoleRequest.setEmail(email);

        if (!user.getRole().equals(request.getRole())) {
            try {
                String url = updateRolePath;
                restTemplate.put(url, updateRoleRequest);
                user.setRole(request.getRole());
            } catch (HttpClientErrorException ex) {
                String responseBody = ex.getResponseBodyAsString();
                ObjectMapper mapper = new ObjectMapper();
                String message = mapper.readTree(responseBody).path("message").asText();
                throw new RuntimeException(message); // ném message gốc
            }
        }

        return save(user);
    }


    public UserResponse findByEmail(String email) {
        return userMapper.UserToUserResponse(userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOTFOUND)));
    }

    public void deleteByEmail(String email) {
        userRepository.deleteByEmail(email);
    }
}
