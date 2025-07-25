package org.example.backend.application.dto;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class CategorySuggestionCommand {
    private String description;     // 交易描述
    private Double amount;          // 金额
    private String currency;        // 币种
    private String transactionType; // 类型：INCOME / EXPENSE
    private Integer companyId;      // 所属公司 ID
}