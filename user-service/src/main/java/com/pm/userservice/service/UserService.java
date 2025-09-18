package com.pm.userservice.service;

import com.pm.userservice.exception.AppException;
import com.pm.userservice.exception.ErrorCode;
import com.pm.userservice.mapper.UserMapper;
import com.pm.userservice.models.User;
import com.pm.userservice.models.dto.request.UserUpdateRequest;
import com.pm.userservice.models.dto.response.UserResponse;
import com.pm.userservice.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public UserResponse save(User user) {
        userRepository.save(user);
        return userMapper.UserToUserResponse(user);
    }

    public UserResponse updateUserProfile(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOTFOUND));

        user.setFullName(request.getFirstname() + " " + request.getLastname());
        user.setAddress(request.getAddress());
        user.setPhone(request.getPhone());

        return save(user);
    }


    public List<UserResponse> findAll() {
        log.info("Find All Users");
        return userRepository.findAll()
                .stream()
                .map(user -> UserResponse.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .fullName(user.getFullName())
                        .build())
                .toList();
    }

    public UserResponse findById(Long id) {
        log.info("Find User by ID {}", id);

        return userMapper.UserToUserResponse(userRepository.findById(id).orElseThrow(()
                -> new AppException(ErrorCode.USER_NOTFOUND)));
    }



    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

}
