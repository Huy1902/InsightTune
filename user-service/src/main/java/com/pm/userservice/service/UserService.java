package com.pm.userservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.userservice.exception.AppException;
import com.pm.userservice.exception.ErrorCode;
import com.pm.userservice.mapper.UserMapper;
import com.pm.userservice.models.User;
import com.pm.userservice.dto.request.UpdateRoleRequest;
import com.pm.userservice.dto.request.UserUpdateRequest;
import com.pm.userservice.dto.response.UserResponse;
import com.pm.userservice.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@Slf4j
public class UserService {
    @Value("${auth-service.update-role}")
    String updateRolePath;

    private final RestTemplate restTemplate;

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CloudinaryService cloudinaryService;

    public UserService(RestTemplate restTemplate, UserRepository userRepository, UserMapper userMapper, CloudinaryService cloudinaryService) {
        this.restTemplate = restTemplate;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.cloudinaryService = cloudinaryService;
    }

    public UserResponse save(User user) {
        userRepository.save(user);
        return userMapper.UserToUserResponse(user);
    }

    /**
     * Cập nhật thông tin profile user.
     * <p>
     * Endpoint này được bảo vệ bởi PostAuthorize, chỉ cho phép cập nhật user trùng với token.
     * </p>
     *
     * @param email email của user (lấy từ token)
     * @param request request chứa thông tin cập nhật
     * @return UserResponse chứa thông tin user sau khi update
     * @throws JsonProcessingException nếu có lỗi khi parse JSON từ response
     */
    @PostAuthorize("returnObject.email == authentication.name")
    public UserResponse updateUserProfile(String email, UserUpdateRequest request) throws JsonProcessingException {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOTFOUND));

        user.setFirstName(request.getFirstname());
        user.setLastName(request.getLastname());
        user.setAddress(request.getAddress());
        user.setPhone(request.getPhone());

        UpdateRoleRequest updateRoleRequest = UpdateRoleRequest.builder()
                        .role(request.getRole())
                        .email(email)
                        .build();

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

    /**
     * Tìm user theo email.
     *
     * @param email email của user
     * @return UserResponse chứa thông tin user
     */
    public UserResponse findByEmail(String email) {
        return userMapper.UserToUserResponse(userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOTFOUND)));
    }

    /**
     * Thay đổi avatar của user.
     *
     * @param email email của user
     * @param avatar file ảnh upload
     * @return URL công khai của avatar mới
     */
    public String changeAvatar(String email, MultipartFile avatar) {
        try {
            String avatarUrl = cloudinaryService.uploadFile(avatar);
            userRepository.changeAvatarByEmail(email, avatarUrl);
            return avatarUrl;
        } catch (IOException ex) {
            throw new AppException(ErrorCode.CANT_UPLOAD_AVATAR);
        }
    }
}
