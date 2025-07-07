package org.example.backend.infrastructure.ai;

import lombok.RequiredArgsConstructor;
import org.example.backend.config.OpenAIConfiguration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OpenAIClient {

    private final OpenAIConfiguration config;
    private final RestTemplate restTemplate = new RestTemplate();

    public String callChatCompletion(String prompt) {
        String url = config.getApiUrl() + "/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(config.getApiKey());

        Map<String, Object> requestBody = Map.of(
                "model", config.getChatModel(),
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "temperature", config.getTemperature(),
                "max_tokens", config.getMaxTokens()
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        return (String) ((Map<String, Object>) choices.get(0).get("message")).get("content");
    }
}
