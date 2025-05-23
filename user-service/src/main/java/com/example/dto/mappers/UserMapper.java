package com.example.dto.mappers;

import com.example.dto.user.UserCreateRequest;
import com.example.dto.user.UserDto;
import com.example.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;

@Mapper(componentModel = ComponentModel.SPRING)
public interface UserMapper {
    User mapToEntity(UserCreateRequest dto);
    UserDto mapToDto(User dto);
}
