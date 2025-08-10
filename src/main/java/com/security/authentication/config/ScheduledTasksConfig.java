package com.security.authentication.config;

import com.security.authentication.repository.RevokedTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasksConfig {

    private final RevokedTokenRepository revokedTokenRepository;

    // Clean up expired revoked tokens every hour
    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    public void cleanupExpiredRevokedTokens() {
        try {
            Timestamp now = Timestamp.from(Instant.now());
            revokedTokenRepository.deleteExpiredTokens(now);
            log.info("Cleaned up expired revoked tokens");
        } catch (Exception e) {
            log.error("Error cleaning up expired revoked tokens", e);
        }
    }
} 