package com.example.repository;

import com.example.entity.ExternalProject;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface ExternalProjectRepository extends CrudRepository<ExternalProject, String> {

    List<ExternalProject> findAll();

    Optional<ExternalProject> findById(String id);
}
