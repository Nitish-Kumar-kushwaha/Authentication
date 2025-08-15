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
        MultipartFile file = resumeRequestDTO.getResumeFile();
        if(file == null || file.isEmpty()) {
            log.error("No file provided or file is empty");
            return ResumeChecker.ResumeResponseDTO.builder()
                    .message("No file provided or file is empty")
                    .build();
        }

        try {
            String resumeText = extractTheTextFromFile(file);
            String score = getScore(resumeText);

            if (score == null) {
                return ResumeChecker.ResumeResponseDTO.builder()
                        .message("Failed to process the resume score")
                        .build();
            }

            return ResumeChecker.ResumeResponseDTO.builder()
                    .message(score)
                    .build();
        } catch (Exception e) {
            log.error("Error processing resume: {}", e.getMessage());
            return ResumeChecker.ResumeResponseDTO.builder()
                    .message("Error processing resume: " + e.getMessage())
                    .build();
        }
    }

    public String getScore(String resumeText){
        String requestBody = ResumeUtils.buildGeminiRequestPayload(resumeText);

        log.info("Request Body: {}", requestBody);

        try {
            JsonNode response = geminiWebClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .timeout(java.time.Duration.ofSeconds(10))
                    .block();

            log.info("Response: {}", response);

            return ResumeUtils.extractJsonFromResponse(response);
        } catch (Exception e) {
            log.error("Error while processing resume score: {}", e.getMessage());
        }
        return null;
    }

    private String extractTheTextFromFile(MultipartFile file) throws Exception{
        try {
            InputStream inputStream = file.getInputStream();
            Tika tika = new Tika();

            return tika.parseToString(inputStream);
        } catch (Exception e) {
            log.error("Error reading file: {}", e.getMessage());
            throw new Exception("Failed to read the file content");
        }
    }
}
