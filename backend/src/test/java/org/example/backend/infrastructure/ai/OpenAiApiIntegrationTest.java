package org.example.backend.infrastructure.ai;

import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class OpenAiApiIntegrationTest {

    private final String openAiUrl = "https://api.openai.com/v1/chat/completions";
    private final RestTemplate restTemplate = new RestTemplate();

    @Test
    void testInvalidApiKeyResponse() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth("INVALID_KEY");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", new Object[]{
            Map.of("role", "user", "content", "Hello")
        });

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        Throwable thrown = catchThrowable(() -> restTemplate.postForEntity(openAiUrl, entity, String.class));

        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("401");
    }

    @Test
    void testInvalidModelName() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth("YOUR_VALID_KEY");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "invalid-model-name");
        requestBody.put("messages", new Object[]{
            Map.of("role", "user", "content", "Hello")
        });

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        Throwable thrown = catchThrowable(() -> restTemplate.postForEntity(openAiUrl, entity, String.class));

        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("400");
    }

    @Test
    void testEmptyRequestBody() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth("YOUR_VALID_KEY");

        HttpEntity<String> entity = new HttpEntity<>("", headers);

        Throwable thrown = catchThrowable(() -> restTemplate.postForEntity(openAiUrl, entity, String.class));

        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("400");
    }
}