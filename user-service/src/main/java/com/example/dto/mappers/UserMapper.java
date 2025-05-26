package com.example.dto.mappers;

import com.example.dto.UserCreateRequest;
import com.example.dto.UserDto;
import com.example.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;

@Mapper(componentModel = ComponentModel.SPRING)
public interface UserMapper {

//    UserMapper INSTANCE = Mappers.getMapper( UserMapper.class );

    @Mapping(target = "externalProjects", ignore = true)
    User mapToEntity(UserCreateRequest dto);
    UserDto mapToDto(User dto);
}
