package org.example.backend.application.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class AIQuestionAnswerResult {
    private String answer;                      // AI生成的财务回答
    private String confidence;                  // 置信度等级（HIGH, MEDIUM, LOW）
    private boolean hasNumericData;             // 是否包含数值分析
    private List<String> dataSources;           // 引用的数据源
    private Map<String, Object> relatedData;    // 相关结构化数据
}