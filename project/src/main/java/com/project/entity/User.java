package com.project.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name="User")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    private Long id;

    private String name;

    private String email;

    private String password;

    private String role;

    private LocalDateTime createdAt;
}
