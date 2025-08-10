package com.security.authentication.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class Login {
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LoginRequestDTO {
        private String username;
        private String password;
        @JsonProperty("user_type")
        private String userType;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LoginResponseDTO {
        private String message;
        private String userId;
        private String refreshToken;
        private String accessToken;
        private List<String> errors;
    }
}
