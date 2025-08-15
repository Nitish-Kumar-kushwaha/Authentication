package com.security.authentication.dto.Resume;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

public class ResumeChecker {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ResumeRequestDTO {
        @JsonProperty("resume")
        MultipartFile resumeFile;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ResumeResponseDTO {
        String message;
    }
}
