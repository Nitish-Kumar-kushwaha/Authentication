package com.security.authentication.controller;

import com.security.authentication.dto.Resume.ResumeChecker;
import com.security.authentication.services.IResumeChecker;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/resume-checker")
@AllArgsConstructor
public class ResumeCheckerController {
    private final IResumeChecker resumeCheckerService;

    @PostMapping("/check-score")
    ResponseEntity<?> checkResumeScore(@RequestParam("resume") MultipartFile resumeFile){
        ResumeChecker.ResumeResponseDTO responseDTO = resumeCheckerService.checkResumeScore(
            ResumeChecker.ResumeRequestDTO.builder()
                .resumeFile(resumeFile)
                .build(), 
            "userId"
        );
        return ResponseEntity.ok(responseDTO);
    }
}
