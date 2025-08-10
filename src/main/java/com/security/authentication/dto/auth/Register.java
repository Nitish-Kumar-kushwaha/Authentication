package com.security.authentication.dto.auth;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class Register {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class RegisterRequestDTO{
        private String username;
        private String password;
        private String email;
        @JsonProperty("user_type")
        private String userType;
        private String role;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RegisterResponseDTO {
        private String message;
        private String userId;
        private List<String> errors;
    }
}
