package org.example.backend.application.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class FinancialQuestionAnswerDTO {
    private String answer;
    private String confidence;
    private boolean hasNumericData;
    private List<String> dataSources;
    private Map<String, Object> relatedData;
}
