package com.security.authentication.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.sql.Timestamp;

@Entity
@Table(name = "revoked_tokens",
       indexes = {
           @Index(name = "idx_revoked_tokens_token_id", columnList = "token_id"),
           @Index(name = "idx_revoked_tokens_expires_at", columnList = "expires_at")
       })
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"users", "adminUser"})
@SuperBuilder
public class RevokedToken extends BaseEntity {

    @Column(name = "token_id", nullable = false, unique = true, length = 255)
    private String tokenId;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "user_id")
    private Users users;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "admin_user_id")
    private AdminUsers adminUser;

    @Column(name = "revoked_at", nullable = false)
    private Timestamp revokedAt;

    @Column(name = "expires_at", nullable = false)
    private Timestamp expiresAt;
} 