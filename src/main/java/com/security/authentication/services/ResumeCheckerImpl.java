package com.security.authentication.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.security.authentication.dto.Resume.ResumeChecker;
import com.security.authentication.util.ResumeUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.InputStream;

@Service
@AllArgsConstructor
@Slf4j
public class ResumeCheckerImpl implements IResumeChecker{
    private final WebClient geminiWebClient;


    @Override
    public ResumeChecker.ResumeResponseDTO checkResumeScore(ResumeChecker.ResumeRequestDTO resumeRequestDTO, String userId) {
        log.info("=== RESUME CHECKER SERVICE CALLED ===");
        log.info("User ID: {}", userId);
        
        MultipartFile file = resumeRequestDTO.getResumeFile();
        if(file == null || file.isEmpty()) {
            log.error("ERROR: No file provided or file is empty");
            return ResumeChecker.ResumeResponseDTO.builder()
                    .message("No file provided or file is empty")
                    .build();
        }

        try {
            log.info("Extracting text from file...");
            String resumeText = extractTheTextFromFile(file);
            log.info("Text extraction completed. Length: {}", resumeText != null ? resumeText.length() : "null");
            
            log.info("Calling getScore method...");
            String score = getScore(resumeText);
            log.info("getScore completed. Score: {}", score != null ? score.substring(0, Math.min(100, score.length())) + "..." : "null");

            if (score == null) {
                log.error("ERROR: getScore returned null");
                return ResumeChecker.ResumeResponseDTO.builder()
                        .message("Failed to process the resume score")
                        .build();
            }

            log.info("Building response DTO...");
            return ResumeChecker.ResumeResponseDTO.builder()
                    .message(score)
                    .build();
        } catch (Exception e) {
            log.error("=== RESUME CHECKER SERVICE ERROR ===");
            log.error("Error: {}", e.getMessage(), e);
            return ResumeChecker.ResumeResponseDTO.builder()
                    .message("Error processing resume: " + e.getMessage())
                    .build();
        }
    }

    public String getScore(String resumeText){
        log.info("=== CALLING GEMINI API ===");
        log.info("Resume text length: {}", resumeText != null ? resumeText.length() : "null");
        
        try {
            log.info("Building Gemini request payload...");
            String requestBody = ResumeUtils.buildGeminiRequestPayload(resumeText);
            log.info("Request payload built successfully. Length: {}", requestBody.length());

            log.info("Request Body: {}", requestBody);

            log.info("Preparing WebClient request...");
            log.info("WebClient instance: {}", geminiWebClient != null ? "NOT NULL" : "NULL");
            
            log.info("Making POST request to Gemini API...");
            JsonNode response = geminiWebClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .timeout(java.time.Duration.ofSeconds(30))
                    .block();

            log.info("Gemini API call completed successfully");
            log.info("Response received: {}", response != null ? "NOT NULL" : "NULL");
            log.info("Response: {}", response);

            log.info("Extracting JSON from response...");
            String extractedJson = ResumeUtils.extractJsonFromResponse(response);
            log.info("JSON extraction completed. Result: {}", extractedJson != null ? extractedJson.substring(0, Math.min(100, extractedJson.length())) + "..." : "null");

            return extractedJson;
        } catch (Exception e) {
            log.error("=== GEMINI API CALL ERROR ===");
            log.error("Error type: {}", e.getClass().getSimpleName());
            log.error("Error message: {}", e.getMessage(), e);
        }
        return null;
    }

    private String extractTheTextFromFile(MultipartFile file) throws Exception{
        log.info("=== TEXT EXTRACTION STARTED ===");
        log.info("File: {} ({} bytes)", file.getOriginalFilename(), file.getSize());
        
        try {
            log.info("Opening input stream...");
            InputStream inputStream = file.getInputStream();
            log.info("Input stream opened successfully");
            
            log.info("Initializing Tika parser...");
            Tika tika = new Tika();
            log.info("Tika parser initialized");

            log.info("Parsing file content...");
            String result = tika.parseToString(inputStream);
            log.info("File parsing completed. Result length: {}", result != null ? result.length() : "null");
            
            return result;
        } catch (Exception e) {
            log.error("=== TEXT EXTRACTION ERROR ===");
            log.error("Error: {}", e.getMessage(), e);
            throw new Exception("Failed to read the file content");
        }
    }


}
