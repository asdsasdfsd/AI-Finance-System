package org.example.backend.application.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class BalanceSheetDetailedResponse {
    private LocalDate asOfDate;
    private Map<String, List<AccountBalanceDTO>> assets;
    private Map<String, List<AccountBalanceDTO>> liabilities;
    private Map<String, List<AccountBalanceDTO>> equity;
    private BigDecimal totalAssets;
    private BigDecimal totalLiabilities;
    private BigDecimal totalEquity;
    private boolean isBalanced;
}

