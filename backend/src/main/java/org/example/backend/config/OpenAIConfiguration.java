package org.example.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ai.openai")
@Data
public class OpenAIConfiguration {
    private String apiKey;
    private String apiUrl;
    private String chatModel;
    private String embeddingModel;
    private Integer maxTokens;
    private Double temperature;
    private Integer timeoutSeconds;
    private Integer maxRetries;
}

