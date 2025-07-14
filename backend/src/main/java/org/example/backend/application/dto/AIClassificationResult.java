package org.example.backend.application.dto;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AIClassificationResult {
    private String category;
    private double confidence;
    private String reason;
    private List<String> alternativeCategories;
    private boolean requireReview;
}
