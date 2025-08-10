package com.security.authentication.entity;

import jakarta.persistence.*;

import java.sql.Timestamp;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "refresh_tokens",
       indexes = {
           @Index(name = "idx_refresh_tokens_token", columnList = "token", unique = true),
           @Index(name = "idx_refresh_tokens_user", columnList = "user_id")
       })
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"users","adminUser"})
@SuperBuilder
public class RefreshToken extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "user_id")
    private Users users;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "admin_user_id")
    private AdminUsers adminUser;

    @Column(name = "token", nullable = false, unique = true, length = 256)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private Timestamp expiresAt;

    @Column(name = "revoked", nullable = false)
    private boolean revoked;

}

