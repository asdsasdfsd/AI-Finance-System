package org.example.backend.infrastructure.ai.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class AIReportInsightResult {
    private String insightSummary;             // 洞察总结
    private List<String> keyFindings;          // 关键发现
    private String confidenceLevel;            // HIGH, MEDIUM, LOW
}
