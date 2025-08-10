package com.security.authentication.entity;

import jakarta.persistence.*;

import java.sql.Timestamp;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "verification_tokens",
       indexes = {
           @Index(name = "idx_verification_tokens_token", columnList = "token", unique = true),
           @Index(name = "idx_verification_tokens_user", columnList = "user_id")
       })
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(exclude = {"user"})
public class VerificationToken extends BaseEntity {

    public enum TokenType {
        EMAIL_VERIFICATION,
        PASSWORD_RESET,
        MFA
    }

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users users;

    @Column(name = "token", nullable = false, unique = true, length = 256)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 32)
    private TokenType type;

    @Column(name = "expires_at", nullable = false)
    private Timestamp expiresAt;

    @Column(name = "used", nullable = false)
    private boolean used;

}

