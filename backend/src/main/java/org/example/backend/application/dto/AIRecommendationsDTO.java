package org.example.backend.application.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AIRecommendationsDTO {
    private List<String> recommendations;   // 推荐建议
    private String reasoning;               // 推荐理由/AI分析解释
    private Double confidence;              // 可信度
    // 可扩展更多字段，如数据源等
}

