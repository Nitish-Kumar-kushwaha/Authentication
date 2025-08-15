package com.security.authentication.util;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for resume-related operations and Gemini API integration.
 * Provides methods for building API payloads and parsing responses.
 */
@Slf4j
public class ResumeUtils {

    /**
     * Extracts JSON content from Gemini API response.
     * Handles markdown-formatted and plain JSON responses.
     *
     * @param response The response from Gemini API
     * @return Clean JSON string without markdown formatting
     */
    public static String extractJsonFromResponse(JsonNode response) {
        log.debug("Starting JSON extraction from Gemini response");

        try {
            if (response == null) {
                log.error("Response is null, cannot extract JSON");
                return "{}";
            }

            log.debug("Response structure: {}", response.toPrettyString());

            // Extract the text content from the response
            String responseText = response.at("/candidates/0/content/parts/0/text").asText();
            log.info("Extracted response text. Length: {} characters",
                    responseText != null ? responseText.length() : "null");

            if (responseText == null || responseText.isEmpty()) {
                log.warn("Empty response text received from Gemini API");
                return "{}";
            }

            log.debug("Response text preview: {}",
                    responseText.length() > 200 ? responseText.substring(0, 200) + "..." : responseText);

            // Strategy 1: Extract JSON from markdown code blocks if present
            if (responseText.contains("```json")) {
                log.debug("Strategy 1: Found markdown code blocks, extracting JSON");
                int startIndex = responseText.indexOf("```json") + 7; // Skip "```json"
                int endIndex = responseText.lastIndexOf("```");
                if (endIndex > startIndex) {
                    String jsonContent = responseText.substring(startIndex, endIndex).trim();
                    log.info("Successfully extracted JSON from markdown. Length: {} characters", jsonContent.length());
                    log.debug("Extracted JSON: {}", jsonContent);
                    return jsonContent;
                } else {
                    log.warn("Markdown code blocks found but end delimiter missing");
                }
            }

            // Strategy 2: Check if response contains "Resume score:" prefix and extract JSON after it
            if (responseText.contains("Resume score:")) {
                log.debug("Strategy 2: Found 'Resume score:' prefix, extracting JSON after it");
                int startIndex = responseText.indexOf("Resume score:") + "Resume score:".length();
                String jsonPart = responseText.substring(startIndex).trim();

                // Try to find the start of JSON (first {)
                int jsonStart = jsonPart.indexOf('{');
                if (jsonStart >= 0) {
                    String jsonContent = jsonPart.substring(jsonStart);
                    log.info("Successfully extracted JSON after 'Resume score:' prefix. Length: {} characters", jsonContent.length());
                    log.debug("Extracted JSON: {}", jsonContent);
                    return jsonContent;
                } else {
                    log.warn("'Resume score:' prefix found but no JSON start brace found");
                }
            }

            // Strategy 3: If no special formatting, try to extract JSON from the response
            log.debug("Strategy 3: No special formatting found, searching for JSON structure");
            int jsonStart = responseText.indexOf('{');
            int jsonEnd = responseText.lastIndexOf('}');
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                String jsonContent = responseText.substring(jsonStart, jsonEnd + 1);
                log.info("Successfully extracted JSON structure. Length: {} characters", jsonContent.length());
                log.debug("Extracted JSON: {}", jsonContent);
                return jsonContent;
            }

            // Strategy 4: If no JSON found, return as is
            log.warn("No JSON structure found in response, returning raw response text");
            log.debug("Raw response text: {}", responseText);
            return responseText;

        } catch (Exception e) {
            log.error("Error extracting JSON from response. Response: {}, Error: {}",
                    response != null ? response.toString() : "null", e.getMessage(), e);
            return "{}";
        }
    }

    /**
     * Builds the request payload for Gemini API.
     *
     * @param resumeText The extracted text from the resume file
     * @return Formatted JSON payload string
     */
    public static String buildGeminiRequestPayload(String resumeText) {
        log.info("Building Gemini request payload for resume text length: {}", resumeText != null ? resumeText.length() : "null");

        try {
            String payload = "{\n" +
                    "    \"contents\": [\n" +
                    "        {\n" +
                    "            \"parts\": [\n" +
                    "                {\n" +
                    "                    \"text\": \"You are a deterministic resume scoring engine. Your output for the same resume MUST be identical every time. Do not deviate. Follow these steps precisely:\\n1. **Analyze Keywords**: Scan the resume for standard software engineering keywords (e.g., Java, Spring Boot, React, SQL, Microservices, Kafka).\\n2. **Analyze Metrics**: Scan the resume for quantifiable achievements (e.g., percentages, numbers like '10x').\\n3. **Analyze Structure**: Check for a standard structure (Summary, Skills, Experience, Education).\\n4. **Calculate Score**: Start with a base of 70. Add 5 points for strong keyword presence. Add 5 points for quantifiable metrics. Add 5 points for good structure.\\n5. **Generate Feedback**: Based ONLY on the analysis above, write a brief, one-sentence feedback. For example, if all points were added, say 'The resume shows strong keyword usage, quantifiable metrics, and a clear structure.'\\n6. **Generate Suggestion**: Based ONLY on the analysis, provide ONE actionable suggestion. If metrics are present, suggest adding more. If the structure is good, suggest tailoring keywords for specific jobs.\\n7. **Final Output**: Provide your response ONLY as a valid JSON object with the keys 'ats_score', 'feedback', and 'suggestion'. Do not include any other text, explanations, or markdown.\\n\\n**Resume:**\\n---begin resume---\\n" + resumeText + "\\n---end resume---\"\n" +
                    "                }\n" +
                    "            ]\n" +
                    "        }\n" +
                    "    ],\n" +
                    "    \"generationConfig\": {\n" +
                    "        \"temperature\": 0.0,\n" +
                    "        \"topK\": 1,\n" +
                    "        \"topP\": 0.0\n" +
                    "    }\n" +
                    "}";

            log.debug("Generated Gemini payload successfully. Payload length: {}", payload.length());
            log.trace("Full payload: {}", payload);

            return payload;

        } catch (Exception e) {
            log.error("Failed to build Gemini request payload. Resume text length: {}, Error: {}",
                    resumeText != null ? resumeText.length() : "null", e.getMessage(), e);
            throw new RuntimeException("Failed to build Gemini request payload", e);
        }
    }

    /**
     * Validates if the response contains valid JSON structure.
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
} 