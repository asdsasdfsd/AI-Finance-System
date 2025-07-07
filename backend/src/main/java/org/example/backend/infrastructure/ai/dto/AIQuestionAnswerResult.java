package org.example.backend.infrastructure.ai.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class AIQuestionAnswerResult {
    private String answer;                     // AI回答
    private List<String> dataSources;          // 数据来源（可选）
    private String confidence;                 // HIGH, MEDIUM, LOW
    private boolean hasNumericData;            // 是否包含数值
    private Map<String, Object> relatedData;   // 相关结构化数据
}
