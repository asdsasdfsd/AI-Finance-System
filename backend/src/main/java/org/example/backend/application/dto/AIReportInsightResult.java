package org.example.backend.application.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class AIReportInsightResult {
    private String insightSummary;                // 分析总结
    private List<String> keyFindings;          // 关键发现点
}

