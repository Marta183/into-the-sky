package com.example.service.impl;

import com.example.dto.mappers.UserMapper;
import com.example.dto.UserCreateRequest;
import com.example.dto.UserDto;
import com.example.entity.User;
import com.example.repository.UserRepository;
import com.example.service.UserService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> findAll() {
        log.info("Finding all users");
        return userRepository.findAll().stream()
                .map(userMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto findById(Long id) {
        log.debug("Finding user by id {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("User not found with id=%d", id)));
        return userMapper.mapToDto(user);
    }

    @Override
    @Transactional
    public UserDto createUser(UserCreateRequest request) {
        log.info("Creating new user with email {}", request.email());

        if (userRepository.findByEmail(request.email()).isPresent()) {
            log.warn("User with email={} already exists", request.email());
            throw new EntityExistsException("Email already exists");
        }

        User user = userMapper.mapToEntity(request);
        user.setEmail(request.email().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.password()));
        User savedUser = userRepository.save(user);

        UserDto userDto = userMapper.mapToDto(savedUser);
        log.info("User created with id {}", savedUser.getId());

        return userDto;
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Attempting to delete user with id {}", id);

        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            log.info("Attempt to remove non existing user with id {}", id);
            return;
        }
        User user = userOptional.get();

        if (!user.getExternalProjects().isEmpty()) {
            log.info("Cannot delete user {} linked to projects", id);
            throw new IllegalStateException(
                    String.format("Cannot delete user with id=%d: linked to %d projects",
                            id, user.getExternalProjects().size()));
        }

        userRepository.deleteById(id);
        log.info("User deleted, id={}", id);
    }
}
