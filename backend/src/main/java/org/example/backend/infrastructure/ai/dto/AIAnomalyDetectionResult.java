package org.example.backend.infrastructure.ai.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AIAnomalyDetectionResult {

    /**
     * 是否被判定为异常
     */
    private boolean isAnomalous;

    /**
     * 异常评分（0.0 - 1.0）
     */
    private Double anomalyScore;

    /**
     * 异常类型（如 金额过大/时间异常 等）
     */
    private String anomalyType;

    /**
     * 异常描述（AI生成的说明）
     */
    private String description;

    /**
     * 推荐的处理建议
     */
    private List<String> recommendations;
}
