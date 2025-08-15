package com.security.authentication.services;

import com.security.authentication.dto.auth.Logout;
import com.security.authentication.dto.auth.Refresh;
import com.security.authentication.dto.auth.Register;
import com.security.authentication.entity.AdminUsers;
import com.security.authentication.entity.RefreshToken;
import com.security.authentication.entity.Users;
import com.security.authentication.entity.RevokedToken;
import com.security.authentication.repository.AdminUserRepository;
import com.security.authentication.repository.RefreshTokenRepository;
import com.security.authentication.repository.RevokedTokenRepository;
import com.security.authentication.repository.UserAccountRepository;
import com.security.authentication.security.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class AuthService {
    private final UserAccountRepository userRepository;
    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RevokedTokenRepository revokedTokenRepository;
    private final JwtService jwtService;

    public AuthService(UserAccountRepository userRepository,
                       AdminUserRepository adminUserRepository,
                       PasswordEncoder passwordEncoder, RefreshTokenRepository refreshTokenRepository, RevokedTokenRepository revokedTokenRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
        this.revokedTokenRepository = revokedTokenRepository;
        this.jwtService = jwtService;
    }

    public Register.RegisterResponseDTO registerUser(Register.RegisterRequestDTO registerRequestDTO) {
        if ("admin_user".equals(registerRequestDTO.getUserType())) {
            String hashedPassword = passwordEncoder.encode(registerRequestDTO.getPassword());
            AdminUsers adminUsers = AdminUsers.builder()
                    .username(registerRequestDTO.getUsername())
                    .email(registerRequestDTO.getEmail())
                    .passwordHash(hashedPassword)
                    .adminRole(AdminUsers.AdminRole.valueOf(registerRequestDTO.getRole().toUpperCase()))
                    .build();
            AdminUsers saved = adminUserRepository.save(adminUsers);
            if (saved == null) {
                return Register.RegisterResponseDTO.builder()
                        .message("Failed to register admin user")
                        .errors(List.of("Database error"))
                        .build();
            }
            return Register.RegisterResponseDTO.builder()
                    .message("Admin user registered successfully")
                    .userId(saved.getId().toString())
                    .build();
        } else {
            String hashedPassword = passwordEncoder.encode(registerRequestDTO.getPassword());
            Users user = Users.builder()
                    .username(registerRequestDTO.getUsername())
                    .email(registerRequestDTO.getEmail())
                    .passwordHash(hashedPassword)
                    .build();
            Users saved = userRepository.save(user);
            if (saved == null) {
                return Register.RegisterResponseDTO.builder()
                        .message("Failed to register user")
                        .errors(List.of("Database error"))
                        .build();
            }
            return Register.RegisterResponseDTO.builder()
                    .message("User registered successfully")
                    .userId(saved.getId().toString())
                    .build();
        }
    }

    public String issueRefreshTokenForIdentifier(String identifier) {
        String[] parts = identifier.split(":");
        String userName = parts[0];
        String userType = parts[1];
        switch (userType.toLowerCase()) {
            case "admin_user":
                AdminUsers admin = adminUserRepository.findByUsername(userName)
                        .orElseThrow(() -> new UsernameNotFoundException("Admin user not found: " + userName));
                RefreshToken adminRefreshToken = RefreshToken.builder()
                        .adminUser(admin)
                        .token(UUID.randomUUID().toString())
                        .expiresAt(Timestamp.from(Instant.now().plus(30, ChronoUnit.DAYS)))
                        .revoked(false)
                        .build();

                refreshTokenRepository.save(adminRefreshToken);
                return adminRefreshToken.getToken();
            case "user":
                Users user = userRepository.findByUsername(userName)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userName));
                RefreshToken userRefreshToken = RefreshToken.builder()
                        .users(user)
                        .token(UUID.randomUUID().toString())
                        .expiresAt(Timestamp.from(Instant.now().plus(30, ChronoUnit.DAYS)))
                        .revoked(false)
                        .build();
                refreshTokenRepository.save(userRefreshToken);
                return userRefreshToken.getToken();
            default:
                throw new IllegalArgumentException("Unknown user type: " + userType);
        }
    }

    @Transactional
    public Logout.LogoutResponseDTO logout(Logout.LogoutRequestDTO logoutRequestDTO) {
        String refreshTokenStr = logoutRequestDTO.getRefreshToken();
        String accessToken = logoutRequestDTO.getAccessToken();
        
        if (refreshTokenStr == null || refreshTokenStr.isEmpty()) {
            return Logout.LogoutResponseDTO.builder()
                    .message("Refresh token is required")
                    .build();
        }
        
        // Revoke refresh token
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new UsernameNotFoundException("Refresh token not found"));

        if (refreshToken.isRevoked()) {
            return Logout.LogoutResponseDTO.builder()
                    .message("Refresh token already revoked")
                    .build();
        }

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
        
        // Blacklist access token if provided
        if (accessToken != null && !accessToken.isEmpty()) {
            String tokenId = jwtService.getTokenId(accessToken);
            if (tokenId != null) {
                // Determine user type and create revoked token record
                String identifier = refreshToken.getAdminUser() != null ? 
                    refreshToken.getAdminUser().getUsername() + ":admin_user" : 
                    refreshToken.getUsers().getUsername() + ":user";
                
                String[] parts = identifier.split(":");
                String username = parts[0];
                String userType = parts[1];
                
                RevokedToken revokedToken = RevokedToken.builder()
                        .tokenId(tokenId)
                        .expiresAt(jwtService.getExpiration(accessToken))
                        .revokedAt(Timestamp.from(Instant.now()))
                        .build();
                
                if ("admin_user".equals(userType)) {
                    revokedToken.setAdminUser(refreshToken.getAdminUser());
                } else {
                    revokedToken.setUsers(refreshToken.getUsers());
                }
                
                revokedTokenRepository.save(revokedToken);
            }
        }

        return Logout.LogoutResponseDTO.builder()
                .message("Logout successful")
                .build();
    }

    public Refresh.RefreshResponseDTO newAccessTokenFromRefresh(String refreshToken) {
        log.info("=== REFRESH TOKEN REQUEST ===");
        log.debug("Refresh token: {}", refreshToken);
        
        try {
            RefreshToken rt = refreshTokenRepository.findByToken(refreshToken)
                    .orElseThrow(() -> new UsernameNotFoundException("Refresh token not found"));
            
            log.info("Found refresh token in database");
            log.debug("Token revoked: {}", rt.isRevoked());
            log.debug("Token expires at: {}", rt.getExpiresAt());
            log.debug("Current time: {}", Instant.now());
            log.debug("Is expired: {}", rt.getExpiresAt().toInstant().isBefore(Instant.now()));

            if (rt.isRevoked() || rt.getExpiresAt().toInstant().isBefore(Instant.now())) {
                log.warn("Refresh token is expired or revoked");
                throw new IllegalArgumentException("Refresh token expired or revoked");
            }

            // Build identifier used by JwtService and UserDetailsService
            String identifier;
            if (rt.getAdminUser() != null) {
                identifier = rt.getAdminUser().getUsername() + ":admin_user";
                log.debug("Admin user: {}", rt.getAdminUser().getUsername());
            } else {
                identifier = rt.getUsers().getUsername() + ":user";
                log.debug("Regular user: {}", rt.getUsers().getUsername());
            }
            
            log.debug("Generated identifier: {}", identifier);

            String access = jwtService.generateAccessToken(identifier, Map.of(
                    "usertype", identifier.endsWith(":admin_user") ? "admin_user" : "user"
            ));
            
            log.info("Generated new access token: {}...", access.substring(0, 50));
            log.info("=== REFRESH TOKEN SUCCESS ===");
            
            return Refresh.RefreshResponseDTO.builder().accessToken(access).build();
            
        } catch (Exception e) {
            log.error("=== REFRESH TOKEN ERROR ===");
            log.error("Error: {}", e.getMessage(), e);
            throw e;
        }
    }
}
