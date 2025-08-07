package org.example.backend.application.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FinancialAnalysisDTO {
    private String summary;                 // 分析摘要
    private List<String> highlights;        // 亮点
    private List<String> risks;             // 风险点
    private List<String> suggestions;       // 优化建议
    // 可根据实际业务继续扩展
}

