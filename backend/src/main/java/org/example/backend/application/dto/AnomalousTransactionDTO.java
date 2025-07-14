package org.example.backend.application.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class AnomalousTransactionDTO {
    private String description;
    private Double amount;
    private LocalDate transactionDate;
    private String category;
    private Double anomalyScore;
    private String anomalyType;
    private List<String> recommendations;
}
