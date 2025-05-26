package com.example.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "external_project", schema = "public")
public class ExternalProject {
    @Id
    @Column(name = "id", length = 200, nullable = false, updatable = false)
    private String id;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @ManyToMany(mappedBy="externalProjects", fetch=FetchType.LAZY)
    private Set<User> users = new HashSet<>();

    public ExternalProject(String name) {
        this.id = generateId();
        this.name = name;
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = generateId();
        }
    }

    private String generateId() {
        return UUID.randomUUID().toString();
    }
}