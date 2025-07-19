// backend/src/main/java/org/example/backend/application/dto/AIQuestionAnswerResult.java
package org.example.backend.application.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class AIQuestionAnswerResult {
    private String answer;                    // AI generated answer
    private String confidence;               // HIGH, MEDIUM, LOW
    private boolean hasNumericData;          // Whether answer contains numeric data
    private List<String> dataSources;        // Data sources used for answer
    private Map<String, Object> relatedData; // Related data and metadata
}