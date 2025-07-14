package org.example.backend.application.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategorySuggestionDTO {
    private String categoryCode;
    private String categoryName;
    private String chineseName;
    private Double confidence;
    private String reason;
}
