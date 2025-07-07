package org.example.backend.infrastructure.ai.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class AIClassificationResult {
    private String suggestedCategory;          // 建议分类，如 OFFICE_EXPENSE
    private Double confidence;                 // AI置信度（0.0 - 1.0）
    private String reason;                     // 推荐原因
    private List<String> alternativeCategories; // 备选分类列表（可选）
    private boolean needsManualReview;         // 是否需要人工审核
}
