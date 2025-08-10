package com.security.authentication.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class Refresh {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RefreshRequestDTO {
        private String refreshToken;
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RefreshResponseDTO {
        private String accessToken;
    }
}
