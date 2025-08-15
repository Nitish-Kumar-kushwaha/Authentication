package com.security.authentication.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GeminiApiConfig {

    @Value("${gemini.api.key}")
    private String geminiApiKey;
    @Value("${gemini.api.base-url}")
    private String geminiBaseUrl;

    @Bean
    public WebClient geminiWebClient(){
        return  WebClient.builder()
                .baseUrl(geminiBaseUrl)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("X-goog-api-key", geminiApiKey)
                .build();
    }
}
