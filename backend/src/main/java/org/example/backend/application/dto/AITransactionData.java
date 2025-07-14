package org.example.backend.application.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
public class AITransactionData {

    private String description;            // 交易描述
    private Double amount;                 // 金额
    private String currency;               // 币种 (e.g. CNY, USD)
    private String category;               // 当前分类
    private LocalDate transactionDate;     // 交易日期
    private String transactionType;        // 类型: INCOME / EXPENSE
    private Integer companyId;             // 所属公司ID
    private Map<String, Object> metadata;  // 其他元信息，如来源、标签等
}