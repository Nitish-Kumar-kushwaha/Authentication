package com.security.authentication.services;

import com.security.authentication.dto.Resume.ResumeChecker;

public interface IResumeChecker {
    ResumeChecker.ResumeResponseDTO checkResumeScore(ResumeChecker.ResumeRequestDTO resumeRequestDTO, String userId);
}
