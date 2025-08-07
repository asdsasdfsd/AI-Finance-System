package org.example.backend.application.dto;

import lombok.Data;

@Data
public class FinancialAnalysisCommand {
    private Integer companyId;
    private String startDate;   // 可用 LocalDate，根据需要
    private String endDate;
    private String reportType;
    private String rawData;     // 原始财务数据内容
}

