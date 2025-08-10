package com.security.authentication.entity;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.time.Instant;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "login_attempts",
       indexes = {
           @Index(name = "idx_login_attempts_user", columnList = "user_id"),
           @Index(name = "idx_login_attempts_ip", columnList = "ip_address")
       })
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(exclude = {"user"})
public class LoginAttempt extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users users;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "successful", nullable = false)
    private boolean successful;

    @Column(name = "occurred_at", nullable = false)
    private Timestamp occurredAt = Timestamp.from(Instant.now());

}

