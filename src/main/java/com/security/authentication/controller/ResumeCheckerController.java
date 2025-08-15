package com.security.authentication.controller;

import com.security.authentication.dto.Resume.ResumeChecker;
import com.security.authentication.services.IResumeChecker;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST Controller for resume analysis and scoring operations.
 * Handles multipart file uploads and delegates to ResumeCheckerService.
 */
@RestController
@RequestMapping("/api/v1/resume-checker")
@AllArgsConstructor
@Slf4j
public class ResumeCheckerController {
    
    /**
     * Service responsible for resume analysis and scoring operations.
     * Injected via constructor using Lombok's @AllArgsConstructor.
     */
    private final IResumeChecker resumeCheckerService;

    /**
     * Analyzes and scores a resume file using AI-powered analysis.
     * Accepts multipart form data and returns AI-generated score with feedback.
     * 
     * @param resumeFile The resume file to analyze
     * @return ResponseEntity containing analysis results or error details
     */
    @PostMapping("/check-score")
    ResponseEntity<?> checkResumeScore(@RequestParam("resume") MultipartFile resumeFile) {
        log.info("=== RESUME CHECKER CONTROLLER CALLED ===");
        log.info("=== RESUME CHECKER API CALL STARTED ===");
        log.info("Received resume file: {} ({} bytes)", 
                resumeFile.getOriginalFilename(), resumeFile.getSize());
        log.info("Content type: {}", resumeFile.getContentType());
        
        try {
            log.info("Calling resume checker service...");
            ResumeChecker.ResumeResponseDTO responseDTO = resumeCheckerService.checkResumeScore(
                ResumeChecker.ResumeRequestDTO.builder()
                    .resumeFile(resumeFile)
                    .build(), 
                "userId" // TODO: Extract actual user ID from JWT token
            );
            
            log.info("Resume checker service completed successfully");
            log.info("Response message length: {}", 
                    responseDTO.getMessage() != null ? responseDTO.getMessage().length() : "null");
            log.debug("Response preview: {}", 
                    responseDTO.getMessage() != null && responseDTO.getMessage().length() > 100 ? 
                    responseDTO.getMessage().substring(0, 100) + "..." : responseDTO.getMessage());
            
            log.info("=== RESUME CHECKER API CALL COMPLETED SUCCESSFULLY ===");
            return ResponseEntity.ok(responseDTO);
            
        } catch (Exception e) {
            log.error("=== RESUME CHECKER CONTROLLER ERROR ===");
            log.error("Error: {}", e.getMessage());
            log.error("=== RESUME CHECKER API CALL FAILED ===");
            log.error("Error in resume checker controller. File: {} ({} bytes). Error: {}", 
                    resumeFile.getOriginalFilename(), resumeFile.getSize(), e.getMessage(), e);
            log.error("Stack trace:", e);
            
            return ResponseEntity.internalServerError().body(
                ResumeChecker.ResumeResponseDTO.builder()
                    .message("Internal server error: " + e.getMessage())
                    .build()
            );
        }
    }
}
