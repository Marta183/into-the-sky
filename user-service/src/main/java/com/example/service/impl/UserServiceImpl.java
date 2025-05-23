package com.example.service.impl;

import com.example.dto.mappers.UserMapper;
import com.example.dto.user.UserCreateRequest;
import com.example.dto.user.UserDto;
import com.example.entity.User;
import com.example.repository.UserRepository;
import com.example.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserDto createUser(UserCreateRequest request) {
//        log.info("Creating new admin {} with e-mail {}", ENTITY_CLASS_NAME, request.getEmail());

        User user = userMapper.mapToEntity(request);
        user.setEmail(request.email().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.password()));
        User savedUser = userRepository.save(user);

        UserDto userDto = userMapper.mapToDto(savedUser);

//        log.info("New admin {} with e-mail {} created in DB", ENTITY_CLASS_NAME, savedUser.getEmail());
        return userDto;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto findById(Long id) {
//        log.debug("Finding {} by ID {}", id);
        UserDto userDto = userRepository.findById(id).map(userMapper::mapToDto)
                .orElseThrow(() -> new EntityNotFoundException("User not found" + id));
//        log.debug("Found {} by ID {}", id);
        return userDto;
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
//        log.debug("Deleting {} with ID {}", ENTITY_CLASS_NAME, id);
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found" + id);
        }
        userRepository.deleteById(id);
//        log.debug("Deleted {} with ID {}", ENTITY_CLASS_NAME, id);
    }
}
