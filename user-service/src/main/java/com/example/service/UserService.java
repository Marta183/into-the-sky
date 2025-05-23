package com.example.service;

import com.example.dto.user.UserCreateRequest;
import com.example.dto.user.UserDto;

public interface UserService {

    UserDto createUser(UserCreateRequest req);

    UserDto findById(Long id);

    void deleteUser(Long id);
}
