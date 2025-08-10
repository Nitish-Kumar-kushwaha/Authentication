package com.security.authentication.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class Logout {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LogoutRequestDTO {
        private String refreshToken;
        private String accessToken;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LogoutResponseDTO {
        private String message;
    }
}
