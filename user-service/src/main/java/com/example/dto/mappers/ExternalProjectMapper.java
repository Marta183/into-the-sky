package com.example.dto.mappers;

import com.example.dto.ExternalProjectDto;
import com.example.entity.ExternalProject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;

@Mapper(componentModel = ComponentModel.SPRING)
public interface ExternalProjectMapper {
//    @Mapping(target = "users", ignore = true)
    ExternalProject mapToEntity(ExternalProjectDto dto);
    ExternalProjectDto mapToDto(ExternalProject dto);
}
