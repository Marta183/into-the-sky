package com.example.service;

import com.example.dto.UserCreateRequest;
import com.example.dto.UserDto;

import java.util.List;

public interface UserService {

    List<UserDto> findAll();

    UserDto createUser(UserCreateRequest req);

    UserDto findById(Long id);

    void deleteUser(Long id);
}
