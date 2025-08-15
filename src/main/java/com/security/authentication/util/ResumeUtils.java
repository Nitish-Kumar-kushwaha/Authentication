package com.security.authentication.util;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for resume-related operations
 */
@Slf4j
public class ResumeUtils {

    /**
     * Extracts JSON content from Gemini API response
     * Handles both markdown-formatted and plain JSON responses
     * 
     * @param response The response from Gemini API
     * @return Clean JSON string without markdown formatting
     */
    public static String extractJsonFromResponse(JsonNode response) {
        try {
            String responseText = response.at("/candidates/0/content/parts/0/text").asText();
            
            if (responseText == null || responseText.isEmpty()) {
                log.warn("Empty response received from Gemini API");
                return "{}";
            }
            
            // Extract JSON from markdown code blocks if present
            if (responseText.contains("```json")) {
                int startIndex = responseText.indexOf("```json") + 7; // Skip "```json"
                int endIndex = responseText.lastIndexOf("```");
                if (endIndex > startIndex) {
                    String jsonContent = responseText.substring(startIndex, endIndex).trim();
                    log.debug("Extracted JSON from markdown: {}", jsonContent);
                    return jsonContent;
                }
            }
            
            // Check if response contains "Resume score:" prefix and extract JSON after it
            if (responseText.contains("Resume score:")) {
                int startIndex = responseText.indexOf("Resume score:") + "Resume score:".length();
                String jsonPart = responseText.substring(startIndex).trim();
                
                // Try to find the start of JSON (first {)
                int jsonStart = jsonPart.indexOf('{');
                if (jsonStart >= 0) {
                    String jsonContent = jsonPart.substring(jsonStart);
                    log.debug("Extracted JSON after 'Resume score:' prefix: {}", jsonContent);
                    return jsonContent;
                }
            }
            
            // If no special formatting, try to extract JSON from the response
            int jsonStart = responseText.indexOf('{');
            int jsonEnd = responseText.lastIndexOf('}');
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                String jsonContent = responseText.substring(jsonStart, jsonEnd + 1);
                log.debug("Extracted JSON from response: {}", jsonContent);
                return jsonContent;
            }
            
            // If no JSON found, return as is
            log.debug("No JSON structure found, returning raw response");
            return responseText;
            
        } catch (Exception e) {
            log.error("Error extracting JSON from response: {}", e.getMessage(), e);
            return "{}";
        }
    }

    /**
     * Builds the request payload for Gemini API
     * 
     * @param resumeText The extracted text from the resume file
     * @return Formatted JSON payload string
     */
    public static String buildGeminiRequestPayload(String resumeText) {
        return """
                {
                    "contents": [
                        {
                            "parts": [
                                {
                                    "text": "You are a deterministic resume scoring engine. Your output for the same resume MUST be identical every time. Do not deviate. Follow these steps precisely:\\\\n1.  **Analyze Keywords**: Scan the resume for standard software engineering keywords (e.g., Java, Spring Boot, React, SQL, Microservices, Kafka).\\\\n2.  **Analyze Metrics**: Scan the resume for quantifiable achievements (e.g., percentages, numbers like '10x').\\\\n3.  **Analyze Structure**: Check for a standard structure (Summary, Skills, Experience, Education).\\\\n4.  **Calculate Score**: Start with a base of 70. Add 5 points for strong keyword presence. Add 5 points for quantifiable metrics. Add 5 points for good structure.\\\\n5.  **Generate Feedback**: Based ONLY on the analysis above, write a brief, one-sentence feedback. For example, if all points were added, say 'The resume shows strong keyword usage, quantifiable metrics, and a clear structure.'\\\\n6.  **Generate Suggestion**: Based ONLY on the analysis, provide ONE actionable suggestion. If metrics are present, suggest adding more. If the structure is good, suggest tailoring keywords for specific jobs.\\\\n7.  **Final Output**: Provide your response ONLY as a valid JSON object with the keys 'ats_score', 'feedback', and 'suggestion'. Do not include any other text, explanations, or markdown.\\\\n\\\\n**Resume:**\\\\n---begin resume---\\\\n%s\\\\n---end resume---"
                                }
                            ]
                        }
                    ],
                    "generationConfig": {
                            "temperature": 0.0,
                            "topK": 1,
                            "topP": 0.0
                    }
                }
                """.formatted(resumeText);
    }

    public static String buildGeminiRequestPayload(String resumeText, String jobDescription) {
        return """
            {
                "contents": [
                    {
                        "parts": [
                            {
                                "text": "You are an advanced, deterministic Applicant Tracking System (ATS) model, designed to emulate the screening process of a top-tier tech company. Your task is to analyze a candidate's resume against a specific job description and calculate a precise alignment score. Your output for the same resume-and-job-description pair MUST be identical every time.\\\\n\\\\n**Scoring Rubric (100 points total):**\\\\n\\\\n1.  **Critical Skill Alignment (50 points):**\\\\n    a. From the Job Description, identify the 5 most critical 'hard skills' (e.g., specific programming languages, frameworks, cloud platforms).\\\\n    b. Award 10 points for each of these 5 skills found in the Resume.\\\\n\\\\n2.  **Experience Relevance (20 points):**\\\\n    a. Analyze the years of experience and job titles in the Resume. Compare them to the requirements in the Job Description (e.g., '5+ years of experience', 'Senior Engineer').\\\\n    b. Award points based on alignment: 20 for a strong match, 10 for a partial match, 0 for a mismatch.\\\\n\\\\n3.  **Impact Metrics (20 points):**\\\\n    a. Scan the Resume's experience section for quantifiable results (e.g., numbers, percentages, dollar amounts, 'X-fold' improvements).\\\\n    b. Award 20 points if at least two strong, quantifiable metrics are found. Award 10 points for one. Award 0 if none are found.\\\\n\\\\n4.  **Education & Certifications (10 points):**\\\\n    a. Check if the Resume's education or certifications match any specific requirements mentioned in the Job Description (e.g., 'BS in Computer Science', 'AWS Certified').\\\\n    b. Award 10 points for a direct match, 0 otherwise.\\\\n\\\\n**Output Generation:**\\\\n1.  **'ats_score'**: The final score is the sum of points from all four factors.\\\\n2.  **'feedback'**: Generate a one-sentence summary of the score breakdown. Example: 'Score breakdown: 40/50 on skills, 20/20 on experience, 10/20 on metrics, and 0/10 on education.'\\\\n3.  **'suggestion'**: Generate ONE specific suggestion targeting the area with the biggest point loss. If skills were the issue, list the missing ones. If metrics were the issue, suggest quantifying a specific project bullet point.\\\\n\\\\nProvide your response ONLY as a valid JSON object. Do not add any extra text or explanations.\\\\n\\\\n---JOB DESCRIPTION START---\\\\n%s\\\\n---JOB DESCRIPTION END---\\\\n\\\\n---RESUME START---\\\\n%s\\\\n---RESUME END---"
                            }
                        ]
                    }
                ],
                "generationConfig": {
                    "temperature": 0.0,
                    "topK": 1
                }
            }
            """.formatted(jobDescription, resumeText);
    }

    /**
     * Validates if the response contains valid JSON structure
     * 
     * @param jsonString The JSON string to validate
     * @return true if valid JSON, false otherwise
     */
    public static boolean isValidJson(String jsonString) {
        try {
            if (jsonString == null || jsonString.trim().isEmpty()) {
                return false;
            }
            
            // Check if it starts and ends with braces
            String trimmed = jsonString.trim();
            if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
                return false;
            }
            
            // Try to parse as JSON to validate structure
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.readTree(jsonString);
            return true;
            
        } catch (Exception e) {
            log.debug("Invalid JSON string: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Formats the response message for better readability
     * 
     * @param jsonResponse The JSON response string
     * @return Formatted response message
     */
    public static String formatResponseMessage(String jsonResponse) {
        if (isValidJson(jsonResponse)) {
            return jsonResponse;
        } else {
            return "Invalid response format: " + jsonResponse;
        }
    }

    /**
     * Extracts specific fields from JSON response
     * 
     * @param jsonString The JSON string
     * @param fieldName The field name to extract
     * @return The field value or null if not found
     */
    public static String extractFieldFromJson(String jsonString, String fieldName) {
        try {
            if (!isValidJson(jsonString)) {
                return null;
            }
            
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            JsonNode jsonNode = mapper.readTree(jsonString);
            
            if (jsonNode.has(fieldName)) {
                return jsonNode.get(fieldName).asText();
            }
            
            return null;
            
        } catch (Exception e) {
            log.error("Error extracting field '{}' from JSON: {}", fieldName, e.getMessage());
            return null;
        }
    }
} 