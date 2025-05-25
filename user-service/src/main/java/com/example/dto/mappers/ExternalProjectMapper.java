package com.example.dto.mappers;

import com.example.dto.ExternalProjectDto;
import com.example.entity.ExternalProject;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;

@Mapper(componentModel = ComponentModel.SPRING)
public interface ExternalProjectMapper {
    ExternalProject mapToEntity(ExternalProjectDto dto);
    ExternalProjectDto mapToDto(ExternalProject dto);
}
