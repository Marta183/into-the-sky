package com.example.dto.mappers;

import com.example.dto.UserCreateRequest;
import com.example.dto.UserDto;
import com.example.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = ComponentModel.SPRING)
public interface UserMapper {

//    UserMapper INSTANCE = Mappers.getMapper( UserMapper.class );

    User mapToEntity(UserCreateRequest dto);
    UserDto mapToDto(User dto);
}
