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
        RefreshToken rt = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new UsernameNotFoundException("Refresh token not found"));

        if (rt.isRevoked() || rt.getExpiresAt().toInstant().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Refresh token expired or revoked");
        }

        // Build identifier used by JwtService and UserDetailsService
        String identifier;
        if (rt.getAdminUser() != null) {
            identifier = rt.getAdminUser().getUsername() + ":admin_user";
        } else {
            identifier = rt.getUsers().getUsername() + ":user";
        }

        String access = jwtService.generateAccessToken(identifier, Map.of(
                "usertype", identifier.endsWith(":admin_user") ? "admin_user" : "user"
        ));
        return Refresh.RefreshResponseDTO.builder().accessToken(access).build();
    }
}
