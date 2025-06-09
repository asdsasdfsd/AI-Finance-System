package org.example.backend.application.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AccountBalanceDTO {
    private String accountName;
    private BigDecimal currentMonth;
    private BigDecimal previousMonth;
    private BigDecimal lastYearEnd;
}
