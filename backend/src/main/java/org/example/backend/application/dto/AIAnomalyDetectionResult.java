package org.example.backend.application.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AIAnomalyDetectionResult {
    private boolean anomalous;
    private double anomalyScore;
    private String anomalyType;
    private List<String> recommendations;
}
