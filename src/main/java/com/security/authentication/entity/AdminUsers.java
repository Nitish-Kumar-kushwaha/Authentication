package com.security.authentication.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.sql.Timestamp;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "admin_users",
       indexes = {
           @Index(name = "idx_admin_users_email", columnList = "email", unique = true),
           @Index(name = "idx_admin_users_username", columnList = "username", unique = true)
       })
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class AdminUsers extends BaseEntity {

    @Column(name = "username", nullable = false, unique = true, length = 64)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "admin_role", nullable = false, length = 20)
    @Builder.Default
    private AdminRole adminRole = AdminRole.ADMIN;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "last_login_at")
    private Timestamp lastLoginAt;

    public enum AdminRole {
        ADMIN,
        SUPER_ADMIN
    }
}

